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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;

import app.Application;

public class PageFileStatistics implements Page {

	public PageFileStatistics() {
		super();
		setPageName("statistics");
		previous = Instant.now();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);

	private List<StatPoint> statPoints = new ArrayList<>();
	private List<Float> pFL = new ArrayList<>(), pFR = new ArrayList<>(), pRL = new ArrayList<>(),
			pRR = new ArrayList<>();
	private List<Float> tFL = new ArrayList<>(), tFR = new ArrayList<>(), tRL = new ArrayList<>(),
			tRR = new ArrayList<>();
	private List<Integer> rainIntensity = new ArrayList<>();
	private List<Integer> trackGripStatus = new ArrayList<>();

	public Map<Integer, StatSession> sessions = new HashMap<>();
	public StatSession currentSession = new StatSession();
	private float fuelBeforePit = 0;
	private float fuelAfterPit = 0;
	private String pageName;
	protected LocalDateTime currentDateAndTime = LocalDateTime.now();
	protected Instant previous, current;
	protected int raceStartAt = 0;
	protected Integer sessionCounter = 0;
	protected Instant lastChange = Instant.now();

	private boolean saved = false;

	public void addStatPoint(StatPoint statPoint) {

		if (statPoint.iCurrentTime != 0) {
			StatPoint prevStatPoint = null;
			if (statPoints.size() > 0) {
				prevStatPoint = statPoints.get(statPoints.size() - 1);
				if (prevStatPoint.iCurrentTime != statPoint.iCurrentTime) {
					if (prevStatPoint.iCurrentTime > statPoint.iCurrentTime && prevStatPoint.lapNo == statPoint.lapNo) {
						// LOGGER.info("Kunos miracle. Skip this data");
					} else {
						currentSession.packetDelta = Math.abs(statPoint.packetIDG - prevStatPoint.packetIDP);
						if (statPoint.lapNo < prevStatPoint.lapNo) {
							LOGGER.info("LAP number is lower");
							currentSession = newSession(statPoint);
						}

						if (statPoint.lapNo < prevStatPoint.lapNo) {
							LOGGER.info("LAP number is lower");
							currentSession = newSession(statPoint);
						}

						if (statPoint.packetIDG < prevStatPoint.packetIDG) {
							LOGGER.info("Data from another session");
							currentSession = newSession(statPoint);
						}

						if (statPoint.packetIDP < prevStatPoint.packetIDP) {
							LOGGER.info("Data from another session");
							currentSession = newSession(statPoint);
						}

						if (statPoint.session != prevStatPoint.session) {
							LOGGER.info("New session");
							currentSession = newSession(statPoint);
						}

						if (statPoint.packetIDG != prevStatPoint.packetIDG
								&& statPoint.packetIDP != prevStatPoint.packetIDP) {
							lastChange = Instant.now();
						}
						if (Duration.between(lastChange, Instant.now()).getSeconds() > 5 && !saved) {
							LOGGER.info("Finished? Save sessions.");
							saveToXLSX();
							saved = true;
						}

						statPoints.add(statPoint);
						if (statPoint.wheelsPressure != null) {
							pFL.add(statPoint.wheelsPressure[0]);
							pFR.add(statPoint.wheelsPressure[1]);
							pRL.add(statPoint.wheelsPressure[2]);
							pRR.add(statPoint.wheelsPressure[3]);
						}
						if (statPoint.tyreCoreTemperature != null) {
							tFL.add(statPoint.tyreCoreTemperature[0]);
							tFR.add(statPoint.tyreCoreTemperature[1]);
							tRL.add(statPoint.tyreCoreTemperature[2]);
							tRR.add(statPoint.tyreCoreTemperature[3]);
						}

						rainIntensity.add(statPoint.rainIntensity);
						trackGripStatus.add(statPoint.trackGripStatus);

						currentSession = sessions.get(sessionCounter);
						// StatSession nextSession = sessions.get(statPoint.sessionIndex + 1);
						// if (currentSession != null && (currentSession.distanceTraveled >
						// statPoint.distanceTraveled)) {
						// LOGGER.info("RESTART??");
						// currentSession = newSession(statPoint);
						// }

						if (prevStatPoint.usedFuel > statPoint.usedFuel
								&& prevStatPoint.distanceTraveled > statPoint.distanceTraveled
								&& prevStatPoint.lapNo == statPoint.lapNo && statPoint.isInPitLane == 1) {
							LOGGER.info("BACK TO PIT??");
							// currentSession = newSession(statPoint);
						} else if (prevStatPoint.distanceTraveled > statPoint.distanceTraveled) {
							LOGGER.info("NEW SESSION??");
							// sessions.clear();
							Gson gson = new Gson();
							System.out.println(gson.toJson(currentSession));
							currentSession = newSession(statPoint);
						}

						if (statPoint.flag == AC_FLAG_TYPE.ACC_GREEN_FLAG
								&& statPoint.session == AC_SESSION_TYPE.AC_RACE && currentSession != null
								&& !currentSession.wasGreenFlag) {
							Gson gson = new Gson();
							System.out.println(gson.toJson(currentSession));
							LOGGER.info("GREEN FLAG, GREEN FLAG, GREEN, GREEN, GREEN!!!!!!!");
							raceStartAt = statPoint.iCurrentTime;
							LOGGER.info("Race start at [ms]: " + String.valueOf(raceStartAt));
							currentSession = newSession(statPoint);
							currentSession.wasGreenFlag = true;
						}

						if (currentSession == null) {
							LOGGER.info("New session");
							currentSession = newSession(statPoint);
						}

						currentSession.sessionTimeLeft = statPoint.sessionTimeLeft;
						currentSession.distanceTraveled = statPoint.distanceTraveled;
						StatLap lap = currentSession.laps.get(statPoint.lapNo);

						if (lap == null) {
							LOGGER.info("New lap started: [" + statPoint.lapNo + "]");
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
							statPoints = new ArrayList<>();
							statPoints.add(statPoint);
							// init new lap, we don't have it in current session
							lap = new StatLap();
							StatLap prevLap = currentSession.laps.get(statPoint.lapNo - 1);
							lap.fuelLeftOnStart = statPoint.fuel;
							lap.maps.put(statPoint.currentMap, 0);
							lap.clockAtStart = statPoint.clock;
							currentSession.laps.put(statPoint.lapNo, lap);
							if (prevLap != null) {
								if (currentSession.wasGreenFlag && currentSession.laps.size() == 2)
									prevLap.first = true;
								LOGGER.info("Fuel at the start of the lap [kg]:" + prevLap.fuelLeftOnStart);
								LOGGER.info("Fuel at the end of the lap [kg]:" + statPoint.fuel);
								currentSession.lastLap = prevLap;
								currentSession.last3Laps.add(prevLap);
								currentSession.last5Laps.add(prevLap);
								if (currentSession.bestLap.lapTime == 0)
									currentSession.bestLap = prevLap;
								else
									currentSession.bestLap = currentSession.bestLap.lapTime < prevLap.lapTime
											? currentSession.bestLap
											: prevLap;
								prevLap.lapTime = statPoint.iLastTime;
								prevLap.splitTimes.put(prevStatPoint.currentSectorIndex, statPoint.iLastTime);
								prevLap.fuelLeftOnEnd = statPoint.fuel;
								prevLap.sessionTimeLeft = prevStatPoint.sessionTimeLeft;
								// currentSession.laps.put(statPoint.sessionIndex, prevLap);
								LOGGER.info("Race start at [ms]: " + String.valueOf(raceStartAt));
								LOGGER.info("Duration [ms]: " + String.valueOf(statPoint.iLastTime));
								LOGGER.info("Calculated duration [ms]: "
										+ String.valueOf(statPoint.iLastTime - raceStartAt));
								long durationInMillis = statPoint.iLastTime - raceStartAt;
								long millis = durationInMillis % 1000;
								long second = (durationInMillis / 1000) % 60;
								long minute = (durationInMillis / (1000 * 60)) % 60;
								long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
								prevLap.lapTime = statPoint.iLastTime - raceStartAt;
								prevLap.fuelXlap = statPoint.fuelXlap;
								prevLap.trackStatus = prevStatPoint.trackStatus;
								LOGGER.info(String.format("Last lap: %02d:%02d:%02d.%d", hour, minute, second, millis));
								calculateLapStats(prevLap, currentSession); // we got new lap, calculate previous
								Gson gson = new Gson();
								if (raceStartAt > 0)
									raceStartAt = 0;
								System.out.println(gson.toJson(prevLap));

							} else {
								// first registered lap
								currentSession.laps.get(statPoint.lapNo).distanceTraveled = statPoint.distanceTraveled;

							}
						}
						if (statPoint.currentSectorIndex != prevStatPoint.currentSectorIndex) {
							if (statPoint.currentSectorIndex > prevStatPoint.currentSectorIndex) {
								lap.splitTimes.put(prevStatPoint.currentSectorIndex, statPoint.lastSectorTime);
							}
						}
						// lap.splitTimes.put(statPoint.currentSectorIndex, statPoint.iSplit);

						currentSession.currentLap = lap;
						lap.lapNo = statPoint.lapNo;
						lap.distanceTraveled = statPoint.distanceTraveled;
						if (statPoint.flag == AC_FLAG_TYPE.ACC_CHECKERED_FLAG) {
							lap.last = true;
						}

						if (statPoint.flag == AC_FLAG_TYPE.ACC_GREEN_FLAG && !currentSession.wasGreenFlag) {
							lap.first = true;
						}

						// we enter the pit
						if (prevStatPoint != null && prevStatPoint.isInPitLane == 0 && statPoint.isInPitLane == 1) {
							fuelBeforePit = statPoint.fuel;
							lap.toPit = true;
						}

						// we left the pit. Did we add some fuel?
						if (prevStatPoint != null && prevStatPoint.isInPitLane == 1 && statPoint.isInPitLane == 0) {
							lap.fromPit = true;
							fuelAfterPit = statPoint.fuel;
							if (fuelAfterPit > fuelBeforePit)
								lap.fuelAdded = fuelAfterPit - fuelBeforePit;
						}

						// lap.fuelLeftOnEnd = Math.min(statPoint.fuel, lap.fuelLeftOnEnd); // keep the
						// lowest value in case of refueling;
						lap.lapTime = statPoint.iCurrentTime;
						lap.isValidLap = !(statPoint.isValidLap == 0); // zero == false

						if (prevStatPoint != null) {
							// sum usage of a map during lap in milliseconds
							Integer currentMapMS = lap.maps.get(statPoint.currentMap);
							if (currentMapMS != null) {
								if (statPoint.currentMap == prevStatPoint.currentMap
										&& statPoint.lapNo == prevStatPoint.lapNo) {
									currentMapMS += 1;
									lap.maps.put(statPoint.currentMap, currentMapMS);
								}
							}

						}
						// session.sessionTimeLeft = statPoint.sessionTimeLeft;
					}
				} else {
					statPoints.add(statPoint);
				}
			} else {
				LOGGER.info("let's get the party started!");
				statPoints.add(statPoint);
			}
		}
	}

	private StatSession newSession(StatPoint statPoint) {
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
				sessions.remove(entry.getKey());
			}
		}

		saveToXLSX();
		saved = false;
		Gson gson = new Gson();
		System.out.println(gson.toJson(sessions));
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

	private void calculateLapStats(StatLap lap, StatSession session) {
		lap.fuelUsed = lap.fuelLeftOnStart - lap.fuelLeftOnEnd;
		float minutes = (float) lap.lapTime / (1000 * 60);
		float perminutes = (lap.fuelUsed + lap.fuelAdded) / minutes;
		if (lap.lapTime > 0)
			lap.fuelAVGPerMinute = perminutes;
		float lavg = 0;
		int avgMS = 0;
		int i = 0;
		Iterator<StatLap> i3laps = session.last3Laps.iterator();
		while (i3laps.hasNext()) {
			i++;
			StatLap l = i3laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
		}
		session.fuelAVG3Laps = lavg / i;
		session.avgLapTime3 = Math.round(avgMS / i);

		lavg = 0;
		avgMS = 0;
		i = 0;
		Iterator<StatLap> i5laps = session.last5Laps.iterator();
		while (i5laps.hasNext()) {
			i++;
			StatLap l = i5laps.next();
			lavg += l.fuelUsed;
			avgMS += l.lapTime;
		}
		session.fuelAVG5Laps = lavg / i;
		session.avgLapTime5 = Math.round(avgMS / i);
		session.fuelAVGPerLap = lap.fuelXlap;
		session.fuelEFNLapsOnEnd = (float) (session.sessionTimeLeft / (1000 * 60)) * perminutes;
		session.fuelNTFOnEnd = session.sessionTimeLeft / session.lastLap.lapTime * lap.fuelUsed;
		// public float fuelEFNMinutesOnEnd = 0;
		OptionalDouble average = pFL.stream().mapToDouble(a -> a).average();
		lap.pFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = pFR.stream().mapToDouble(a -> a).average();
		lap.pFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = pRL.stream().mapToDouble(a -> a).average();
		lap.pRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = pRR.stream().mapToDouble(a -> a).average();
		lap.pRR = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = tFL.stream().mapToDouble(a -> a).average();
		lap.tFL = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = tFR.stream().mapToDouble(a -> a).average();
		lap.tFR = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = tRL.stream().mapToDouble(a -> a).average();
		lap.tRL = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = tRR.stream().mapToDouble(a -> a).average();
		lap.tRR = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = rainIntensity.stream().mapToDouble(a -> a).average();
		lap.rainIntensity = (float) (average.isPresent() ? average.getAsDouble() : 0);

		average = trackGripStatus.stream().mapToDouble(a -> a).average();
		lap.trackGripStatus = (float) (average.isPresent() ? average.getAsDouble() : 0);

		pFL = new ArrayList<>();
		pFR = new ArrayList<>();
		pRL = new ArrayList<>();
		pRR = new ArrayList<>();
		tFL = new ArrayList<>();
		tFR = new ArrayList<>();
		tRL = new ArrayList<>();
		tRR = new ArrayList<>();
		rainIntensity = new ArrayList<>();
		trackGripStatus = new ArrayList<>();

		LOGGER.info("fuelNTFOnEnd: " + session.fuelNTFOnEnd);
		LOGGER.info("Full laps +1 left (based on last lap): " + session.sessionTimeLeft / session.lastLap.lapTime);
		LOGGER.info("Full laps +1 left (based on best lap): " + session.sessionTimeLeft / session.bestLap.lapTime);
	}

	public void saveToXLSX() {
		Workbook workbook = new XSSFWorkbook();
		Iterator<Map.Entry<Integer, StatSession>> iterator = sessions.entrySet().iterator();

		while (iterator.hasNext()) {
			int rowNo = 0;
			Map.Entry<Integer, StatSession> entry = iterator.next();
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
			headerCell.setCellValue("fuelLeftOnStart");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(6);
			headerCell.setCellValue("fuelLeftOnEnd");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(7);
			headerCell.setCellValue("fuelAVGPerMinute");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(8);
			headerCell.setCellValue("fuelXlap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(9);
			headerCell.setCellValue("fuelAdded");
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
				cell.setCellValue(lap.getValue().lapTime);
				cell.setCellStyle(style);

				if (lap.getValue().splitTimes.get(0) != null) {
					cell = row.createCell(2);
					cell.setCellValue(lap.getValue().splitTimes.get(0));
					cell.setCellStyle(style);
				}
				if (lap.getValue().splitTimes.get(1) != null) {
					cell = row.createCell(3);
					cell.setCellValue(lap.getValue().splitTimes.get(1));
					cell.setCellStyle(style);
				}
				if (lap.getValue().splitTimes.get(2) != null) {
					cell = row.createCell(4);
					cell.setCellValue(lap.getValue().splitTimes.get(2));
					cell.setCellStyle(style);
				}

				cell = row.createCell(5);
				cell.setCellValue(lap.getValue().fuelLeftOnStart);
				cell.setCellStyle(style);
				cell = row.createCell(6);
				cell.setCellValue(lap.getValue().fuelLeftOnEnd);
				cell.setCellStyle(style);
				cell = row.createCell(7);
				cell.setCellValue(lap.getValue().fuelAVGPerMinute);
				cell.setCellStyle(style);
				cell = row.createCell(8);
				cell.setCellValue(lap.getValue().fuelXlap);
				cell.setCellStyle(style);
				cell = row.createCell(9);
				cell.setCellValue(lap.getValue().fuelAdded);
				cell.setCellStyle(style);

				cell = row.createCell(10);
				cell.setCellValue(lap.getValue().pFL);
				cell.setCellStyle(style);
				cell = row.createCell(11);
				cell.setCellValue(lap.getValue().pFR);
				cell.setCellStyle(style);
				cell = row.createCell(12);
				cell.setCellValue(lap.getValue().pRL);
				cell.setCellStyle(style);
				cell = row.createCell(13);
				cell.setCellValue(lap.getValue().pRR);
				cell.setCellStyle(style);

				cell = row.createCell(14);
				cell.setCellValue(lap.getValue().tFL);
				cell.setCellStyle(style);
				cell = row.createCell(15);
				cell.setCellValue(lap.getValue().tFR);
				cell.setCellStyle(style);
				cell = row.createCell(16);
				cell.setCellValue(lap.getValue().tRL);
				cell.setCellStyle(style);
				cell = row.createCell(17);
				cell.setCellValue(lap.getValue().tRR);
				cell.setCellStyle(style);
				cell = row.createCell(18);
				cell.setCellValue(lap.getValue().rainIntensity);
				cell.setCellStyle(style);
				cell = row.createCell(19);
				cell.setCellValue(lap.getValue().rainTyres);
				cell.setCellStyle(style);
				cell = row.createCell(20);
				cell.setCellValue(lap.getValue().trackGripStatus);
				cell.setCellStyle(style);
				cell = row.createCell(21);
				cell.setCellValue(lap.getValue().trackStatus);
				cell.setCellStyle(style);
				cell = row.createCell(22);
				cell.setCellValue(lap.getValue().isValidLap);
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
		System.out.println(fileLocation);

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

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

	@Override
	public String toJSON(List<String> fields) {
		String response = "";
		Page page = this;
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
