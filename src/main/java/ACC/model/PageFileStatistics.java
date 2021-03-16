package ACC.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;

import app.Application;

@JsonFilter("filter1")
public class PageFileStatistics implements Page {

	public PageFileStatistics() {
		super();
		setPageName("statistics");
		previous = Instant.now();
	}
	@JsonIgnore
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);
	@JsonIgnore
	public Map<Integer, StatSession> sessions = new HashMap<>();
	
	public StatSession currentSession = new StatSession();
	private String pageName;
	@JsonIgnore
	protected LocalDateTime currentDateAndTime = LocalDateTime.now();
	@JsonIgnore
	protected Instant previous, current;
	
	protected int raceStartAt = 0;
	@JsonIgnore
	protected Integer sessionCounter = 0;
	@JsonIgnore
	protected Instant lastChange = Instant.now();
	
	@JsonIgnore
	private boolean saved = false;
 
	public static String mstoStr(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%03d",hour, minute, second, millis);
	}
	
	public void addStatPoint(StatPoint statPoint) {
		StatPoint prevStatPoint = null;
		currentSession = sessions.get(sessionCounter);

		if (currentSession == null) {
			LOGGER.info("New session");
			currentSession = newSession(statPoint);
		}

		if (statPoint.iCurrentTime != 0) {
			if (currentSession.currentLap.statPoints.size() > 0) {
				prevStatPoint = currentSession.currentLap.statPoints
						.get(currentSession.currentLap.statPoints.size() - 1);
				currentSession.currentLap.addStatPoint(statPoint);
				if (statPoint.packetIDG != prevStatPoint.packetIDG && statPoint.packetIDP != prevStatPoint.packetIDP) {
					lastChange = Instant.now();
				}
				if (prevStatPoint.iCurrentTime != statPoint.iCurrentTime) {
					if (prevStatPoint.iCurrentTime > statPoint.iCurrentTime && prevStatPoint.lapNo == statPoint.lapNo) {
						// LOGGER.info("Kunos miracle. Skip this data");
					} else {
						currentSession.packetDelta = Math.abs(statPoint.packetIDG - prevStatPoint.packetIDP);
						if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
							LOGGER.info("Finished? Save sessions.");
							saveToXLSX();
							saved = true;
						}

						if (newSessionStarted(prevStatPoint, statPoint))
							currentSession = newSession(statPoint);

						if (statPoint.flag == AC_FLAG_TYPE.ACC_GREEN_FLAG
								&& statPoint.session == AC_SESSION_TYPE.AC_RACE && currentSession != null
								&& !currentSession.wasGreenFlag) {
							// System.out.println(gson.toJson(currentSession));
							LOGGER.info("GREEN FLAG, GREEN FLAG, GREEN, GREEN, GREEN!!!!!!!");
							raceStartAt = statPoint.iCurrentTime;
							LOGGER.info("Race start at [ms]: " + String.valueOf(raceStartAt));
							currentSession = newSession(statPoint);
							currentSession.wasGreenFlag = true;
							currentSession.currentLap.first = true;
						}

						StatLap lap = currentSession.laps.get(statPoint.lapNo);

						if (lap == null) {
							if (statPoint.normalizedCarPosition <= 1) {
								LOGGER.info("New lap started: [" + statPoint.lapNo + "]");
								LOGGER.info("Car position: [" + statPoint.normalizedCarPosition + "]");
								StatLap prevLap = currentSession.laps.get(statPoint.lapNo - 1);
								if (prevLap != null) {
									prevLap.lapTime = statPoint.iLastTime;
									prevLap.splitTimes.put(statPoint.car.sectorCount - 1, statPoint.iLastTime);
									prevLap.lapTime = statPoint.iLastTime;
								}

								long duration = 0;
								current = Instant.now();
								if (previous != null) {
									duration = ChronoUnit.MILLIS.between(previous, current);
									long durationofLap = statPoint.iLastTime;
									if (durationofLap != 0) {
										DecimalFormat df = new DecimalFormat("#.##");
										LOGGER.info("Efficiency: " + df.format((float) duration / durationofLap * 100));
										previous = Instant.now();
									}
								}

								// init new lap, we don't have it in current session
								lap = new StatLap();
								lap.addStatPoint(statPoint);
								currentSession.addStatLap(lap);
								currentSession.addStatPoint(statPoint);
							}
						} else {
							if (statPoint.currentSectorIndex > 0)
								lap.splitTimes.put(statPoint.currentSectorIndex - 1, statPoint.lastSectorTime);
							if (lap.fuelLeftOnStart == 0 && statPoint.fuel != 0 && lap.lapNo == statPoint.lapNo)
								lap.fuelLeftOnStart = statPoint.fuel;

						}
						currentSession.currentLap = lap;
					}
				} else {
					currentSession.currentLap.addStatPoint(statPoint);
					currentSession.addStatPoint(statPoint);
					if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
						LOGGER.info("Finished? Save sessions.");
						saveToXLSX();
						saved = true;
					}
				}
			} else {
				LOGGER.info("let's get the party started!");
				if (currentSession.currentLap.statPoints.size() == 0)
					currentSession.currentLap.addStatPoint(statPoint);
				else
					if (currentSession.currentLap.statPoints.get(currentSession.currentLap.statPoints.size()-1).normalizedCarPosition > statPoint.normalizedCarPosition )
						currentSession.currentLap.addStatPoint(statPoint);
			}
		}
	}

	private boolean newSessionStarted(StatPoint prevStatPoint, StatPoint currentStatPoint) {
		boolean newSession = false;
		if (currentStatPoint.lapNo < prevStatPoint.lapNo) {
			LOGGER.info("LAP number is lower");
			newSession = true;
		}

		if (currentStatPoint.lapNo < prevStatPoint.lapNo) {
			LOGGER.info("LAP number is lower");
			newSession = true;
		}

		if (currentStatPoint.packetIDG < prevStatPoint.packetIDG) {
			LOGGER.info("Data from another session");
			newSession = true;
		}

		if (currentStatPoint.packetIDP < prevStatPoint.packetIDP) {
			LOGGER.info("Data from another session");
			newSession = true;
		}

		if (currentStatPoint.session != prevStatPoint.session) {
			LOGGER.info("New session");
			newSession = true;
		}
		
		if (prevStatPoint.distanceTraveled > currentStatPoint.distanceTraveled && prevStatPoint.lapNo > currentStatPoint.lapNo) {
			LOGGER.info("Distance traveled is lower: IT IS A NEW SESSION");
			newSession = true;
		}
		
		return newSession;
	}
	
	private StatSession newSession(StatPoint statPoint) {
		List<Integer> sessionsToRemove = new ArrayList<>();
		Iterator<Map.Entry<Integer, StatSession>> iterator = sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, StatSession> entry = iterator.next();
			if (entry.getValue().laps != null) {
				Iterator<Map.Entry<Integer, StatLap>> iteratorLap = entry.getValue().laps.entrySet().iterator();
				int i = 0;
				while (iteratorLap.hasNext()) {
					Map.Entry<Integer, StatLap> lap = iteratorLap.next();
					if (lap.getValue().splitTimes.size() != entry.getValue().car.sectorCount) {
						entry.getValue().laps.remove(lap.getKey());
					}
				}
			}
			if (entry.getValue().laps.size() == 0) {
				sessionsToRemove.add(entry.getKey());
			}
		}
		
		sessionsToRemove.forEach( key -> {
			sessions.remove(key);
		});

		//saveToXLSX();
		saved = false;
		Gson gson = new Gson();
		//System.out.println(gson.toJson(sessions));
		sessionCounter++;
		currentSession = new StatSession();
		currentSession.internalSessionIndex = sessionCounter;
		currentSession.sessionIndex = statPoint.sessionIndex;
		currentSession.session_TYPE = statPoint.session;

		switch (statPoint.session) {
		case AC_SESSION_TYPE.AC_QUALIFY:
			LOGGER.info("Session type: QUALIFY");
			break;
		case AC_SESSION_TYPE.AC_PRACTICE:
			LOGGER.info("Session type: PRACTICE");
			break;
		case AC_SESSION_TYPE.AC_RACE:
			LOGGER.info("Session type: RACE");
			break;
		case AC_SESSION_TYPE.AC_HOTLAP:
			LOGGER.info("Session type: HOTLAP");
			break;
		}

		currentSession.car = statPoint.car;
		LOGGER.info(currentSession.car.carModel + " max tank: [" + currentSession.car.maxFuel + "]");
		LOGGER.info(currentSession.car.track);
		LOGGER.info(currentSession.car.playerName);

		sessions.put(sessionCounter, currentSession);
		return currentSession;
	}

	

	public void saveToXLSX() {
		Workbook workbook = new XSSFWorkbook();
		Iterator<Map.Entry<Integer, StatSession>> iterator = sessions.entrySet().iterator();
		DecimalFormat df = new DecimalFormat("0.000");
		List<Integer> sessionsToRemove = new ArrayList<>();
		while (iterator.hasNext()) {
			int rowNo = 0;
			Map.Entry<Integer, StatSession> entry = iterator.next();
			if (iterator.hasNext())
				sessionsToRemove.add(entry.getKey());
			
			Sheet sheet = workbook.createSheet("Session " + entry.getValue().internalSessionIndex);
			sheet.setColumnWidth(0, 5000);
			sheet.setColumnWidth(1, 6000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 6000);
			sheet.setColumnWidth(4, 6000);
			sheet.setColumnWidth(5, 6000);
			sheet.setColumnWidth(6, 6000);
			sheet.setColumnWidth(7, 6000);

			Row header = sheet.createRow(rowNo++);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 12);
			font.setBold(true);
			headerStyle.setFont(font);

			Cell headerCell = header.createCell(0);
			headerCell.setCellValue("Track");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(1);
			headerCell.setCellValue("Car");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(2);
			headerCell.setCellValue("Driver name");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(3);
			headerCell.setCellValue("Session type");
			headerCell.setCellStyle(headerStyle);
			// DATA
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			Row row = sheet.createRow(rowNo++);
			Cell cell = row.createCell(0);
			cell.setCellValue(entry.getValue().car.track);
			cell.setCellStyle(style);

			cell = row.createCell(1);
			cell.setCellValue(entry.getValue().car.carModel);
			cell.setCellStyle(style);

			cell = row.createCell(2);
			cell.setCellValue(entry.getValue().car.playerName);
			cell.setCellStyle(style);

			cell = row.createCell(3);
			switch (entry.getValue().session_TYPE) {
			case AC_SESSION_TYPE.AC_QUALIFY:
				cell.setCellValue("QUALIFY");
				break;
			case AC_SESSION_TYPE.AC_PRACTICE:
				cell.setCellValue("PRACTICE");
				break;
			case AC_SESSION_TYPE.AC_RACE:
				cell.setCellValue("RACE");
				break;
			case AC_SESSION_TYPE.AC_HOTLAP:
				cell.setCellValue("HOTLAP");
				break;
			}
			cell.setCellStyle(style);

			row = sheet.createRow(rowNo++);
			header = sheet.createRow(rowNo++);
			header = sheet.createRow(rowNo++);

			headerCell = header.createCell(0);
			headerCell.setCellValue("Recorded Laps");
			headerCell.setCellStyle(headerStyle);

			row = sheet.createRow(rowNo++);

			headerCell = header.createCell(0);
			headerCell.setCellValue("lapNo");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(1);
			headerCell.setCellValue("lapTime");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(2);
			headerCell.setCellValue("splitTime 1");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(3);
			headerCell.setCellValue("splitTime 2");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(4);
			headerCell.setCellValue("splitTime 3");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(5);
			headerCell.setCellValue("Fuel start lap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(6);
			headerCell.setCellValue("Fuel end lap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(7);
			headerCell.setCellValue("l/min");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(8);
			headerCell.setCellValue("l/lap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(9);
			headerCell.setCellValue("refuel [l]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(10);
			headerCell.setCellValue("pFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(11);
			headerCell.setCellValue("pFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(12);
			headerCell.setCellValue("pRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(13);
			headerCell.setCellValue("pRR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(14);
			headerCell.setCellValue("tFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(15);
			headerCell.setCellValue("tFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(16);
			headerCell.setCellValue("tRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(17);
			headerCell.setCellValue("tRR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(18);
			headerCell.setCellValue("rainIntensity");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(19);
			headerCell.setCellValue("rainTyres");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(20);
			headerCell.setCellValue("trackGripStatus");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(21);
			headerCell.setCellValue("trackStatus");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(22);
			headerCell.setCellValue("validLap");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(23);
			headerCell.setCellValue("airTemp");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(24);
			headerCell.setCellValue("roadTemp");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(25);
			headerCell.setCellValue("Fuel for next [lap]");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(26);
			headerCell.setCellValue("Fuel for next [time]");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(27);
			headerCell.setCellValue("Seesion time left");
			headerCell.setCellStyle(headerStyle);

			Iterator<Map.Entry<Integer, StatLap>> iteratorLap = entry.getValue().laps.entrySet().iterator();
			int i = 0;
			while (iteratorLap.hasNext()) {
				Map.Entry<Integer, StatLap> lap = iteratorLap.next();
				row = sheet.createRow(rowNo++);
				cell = row.createCell(0);
				cell.setCellValue(lap.getValue().lapNo);
				if (lap.getValue().first) {
					style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
					cell = row.createCell(10);
					cell.setCellValue("First lap");
					cell.setCellStyle(style);
				} else if (lap.getValue().last) {
					style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
					cell = row.createCell(10);
					cell.setCellValue("Last lap");
					cell.setCellStyle(style);
				} else {
					style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
					cell.setCellStyle(style);
				}

				cell.setCellStyle(style);

				cell = row.createCell(1);
				cell.setCellValue(mstoStr(lap.getValue().lapTime));
				cell.setCellStyle(style);

				if (lap.getValue().splitTimes.get(0) != null) {
					cell = row.createCell(2);
					cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(0)));
					cell.setCellStyle(style);
				}
				if (lap.getValue().splitTimes.get(1) != null) {
					cell = row.createCell(3);
					cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(1)-lap.getValue().splitTimes.get(0)));
					cell.setCellStyle(style);
				}
				if (lap.getValue().splitTimes.get(2) != null) {
					cell = row.createCell(4);
					cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(2)-lap.getValue().splitTimes.get(1)));
					cell.setCellStyle(style);
				}

				cell = row.createCell(5);
				cell.setCellValue(df.format(lap.getValue().fuelLeftOnStart));
				cell.setCellStyle(style);
				cell = row.createCell(6);
				cell.setCellValue(df.format(lap.getValue().fuelLeftOnEnd));
				cell.setCellStyle(style);
				cell = row.createCell(7);
				cell.setCellValue(df.format(lap.getValue().fuelAVGPerMinute));
				cell.setCellStyle(style);
				cell = row.createCell(8);
				cell.setCellValue(df.format(lap.getValue().fuelXlap));
				cell.setCellStyle(style);
				cell = row.createCell(9);
				cell.setCellValue(df.format(lap.getValue().fuelAdded));
				cell.setCellStyle(style);

				boolean wet = lap.getValue().rainTyres == 1 ? true : false;
				
				cell = row.createCell(10);
				cell.setCellValue(df.format(lap.getValue().avgpFL));
				cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpFL, wet));
				cell = row.createCell(11);
				cell.setCellValue(df.format(lap.getValue().avgpFR));
				cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpFR, wet));
				cell = row.createCell(12);
				cell.setCellValue(df.format(lap.getValue().avgpRL));
				cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpRL, wet));
				cell = row.createCell(13);
				cell.setCellValue(df.format(lap.getValue().avgpRR));
				cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpRR, wet));

				cell = row.createCell(14);
				cell.setCellValue(df.format(lap.getValue().avgtFL));
				cell.setCellStyle(style);
				cell = row.createCell(15);
				cell.setCellValue(df.format(lap.getValue().avgtFR));
				cell.setCellStyle(style);
				cell = row.createCell(16);
				cell.setCellValue(df.format(lap.getValue().avgtRL));
				cell.setCellStyle(style);
				cell = row.createCell(17);
				cell.setCellValue(df.format(lap.getValue().avgtRR));
				cell.setCellStyle(style);
				cell = row.createCell(18);
				cell.setCellValue(df.format(lap.getValue().avgRainIntensity));
				cell.setCellStyle(style);
				cell = row.createCell(19);
				cell.setCellValue(lap.getValue().rainTyres);
				cell.setCellStyle(style);
				cell = row.createCell(20);
				cell.setCellValue(lap.getValue().avgTrackGripStatus);
				cell.setCellStyle(style);
				cell = row.createCell(21);
				cell.setCellValue(lap.getValue().trackStatus);
				cell.setCellStyle(style);
				cell = row.createCell(22);
				cell.setCellValue(lap.getValue().isValidLap);
				cell.setCellStyle(style);
				cell = row.createCell(23);
				cell.setCellValue(df.format(lap.getValue().avgAirTemp));
				cell.setCellStyle(style);
				cell = row.createCell(24);
				cell.setCellValue(df.format(lap.getValue().avgRoadTemp));
				cell.setCellStyle(style);
				cell = row.createCell(25);
				cell.setCellValue(df.format(lap.getValue().fuelEFNLapsOnEnd));
				cell.setCellStyle(style);
				cell = row.createCell(26);
				cell.setCellValue(mstoStr(Math.round(lap.getValue().fuelEstForNextMiliseconds)));
				cell.setCellStyle(style);
				cell = row.createCell(27);
				cell.setCellValue(mstoStr(Math.round(lap.getValue().sessionTimeLeft)));
				cell.setCellStyle(style);
				
				/*
				 * Iterator<Map.Entry<Integer, Integer>> iteratorMap =
				 * lap.getValue().maps.entrySet().iterator(); int c = 22; int sum = 0; float
				 * percent = 0; while (iteratorMap.hasNext()) { Map.Entry<Integer, Integer> map
				 * = iteratorMap.next(); sum += map.getValue().intValue(); } if (sum > 0) {
				 * iteratorMap = lap.getValue().maps.entrySet().iterator(); while
				 * (iteratorLap.hasNext()) { Map.Entry<Integer, Integer> map =
				 * iteratorMap.next(); percent = (float) (map.getValue() / sum * 100.0); cell =
				 * row.createCell(c); cell.setCellValue(percent); cell.setCellStyle(style); c++;
				 * } }
				 */
			}

		}
		// SAVE
		String pattern = "yyyy_MM_dd_HH_mm_ss";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		LocalDateTime now = LocalDateTime.now();
		String nowDate = now.format(formatter);

		// Writing to a file

		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + nowDate + ".xlsx";
		LOGGER.info(fileLocation);

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sessionsToRemove.forEach( key -> {
			sessions.remove(key);
		});
	}

	@Override
	public String getPageName() {
		return pageName;
	}

	@Override
	public void setPageName(String pageName) {
		this.pageName = pageName;

	}

	@Override
	public String toJSON() {

		String response = "";
		Page page = this;
		try {
			FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
					SimpleBeanPropertyFilter.serializeAllExcept(""));
			ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
			response = mapper.writeValueAsString(page);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}

	private CellStyle pressureXLSXStyle(Workbook workbook, float psi, boolean wet) {
		
		CellStyle coldStyle = workbook.createCellStyle();
		coldStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		coldStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		CellStyle normalStyle = workbook.createCellStyle();
		normalStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		normalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		CellStyle warmStyle = workbook.createCellStyle();
		warmStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		warmStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		CellStyle hotStyle = workbook.createCellStyle();
		hotStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
		hotStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		if (!wet) {
			if (psi < 27.5) return coldStyle; 
				else if (psi >= 27.5 && psi <= 28.0 ) return normalStyle; 
					else if (psi > 28.0 && psi < 28.5) return warmStyle; 
						else return hotStyle;
		} else {
			return normalStyle;
		}
	}
	
	@Override
	public String toJSON(List<String> fields) {
		String response = "";
		PageFileStatistics page = this;
		try {
			Set<String> fieldsFilter = new HashSet<String>(fields);
			FilterProvider filters = new SimpleFilterProvider().addFilter("filter1",
					SimpleBeanPropertyFilter.filterOutAllExcept(fieldsFilter));
			ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
			response = mapper.writeValueAsString(page);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}

	@Override
	public boolean isACCConnected() {
		return true;
	}
}
