package ACC.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
		System.out.println("New Statistics Page");
	}

	private List<StatPoint> statPoints = new ArrayList<>();
	public Map<Integer, StatSession> sessions = new HashMap<>();
	private float fuelBeforePit = 0;
	private float fuelAfterPit = 0;
	private String pageName;

	public void addStatPoint(StatPoint statPoint) {
		StatPoint prevStatPoint = null;
		if (statPoints.size() > 0) {
			prevStatPoint = statPoints.get(statPoints.size() - 1);
			if (prevStatPoint.iCurrentTime > statPoint.iCurrentTime && prevStatPoint.lapNo == statPoint.lapNo ) {
				System.out.println("Kunos miracle");
			}
			//System.out.println("CURR" + statPoint.iCurrentTime + " PREV: " + prevStatPoint.iCurrentTime);

		}
		
		
		statPoints.add(statPoint);
		StatSession session = sessions.get(statPoint.sessionIndex);
		StatSession nextSession = sessions.get(statPoint.sessionIndex + 1);
		if (nextSession != null) {
			System.out.println("RESTART??");
			// started again?
			sessions.clear();
			Gson gson = new Gson();
			System.out.println(gson.toJson(session));
			session = null;
		}
		if (session == null) {
			System.out.println("New session");
			session = new StatSession();
			sessions.put(statPoint.sessionIndex, session);
		}
		session.car = statPoint.car;
		session.session_TYPE = statPoint.session;
		StatLap lap = session.laps.get(statPoint.lapNo);

		if (lap == null) {
			System.out.println("New lap [number]: " + statPoint.lapNo);
			// init new lap, we don't have it in current session
			lap = new StatLap();
			StatLap prevLap = session.laps.get(statPoint.lapNo - 1);
			lap.fuelLeftOnStart = statPoint.fuel;
			lap.maps.put(statPoint.currentMap, 0);
			System.out.println("Fuel start:" + statPoint.fuel);
			session.laps.put(statPoint.lapNo, lap);
			if (prevLap != null) {
				
				session.last3Laps.add(prevLap);
				session.last5Laps.add(prevLap);
				session.bestLap = session.bestLap.lapTime < prevLap.lapTime ? session.bestLap : prevLap;
				prevLap.fuelLeftOnEnd = statPoint.fuel;
				session.laps.put(statPoint.sessionIndex, prevLap);
				
				long durationInMillis = statPoint.iLastTime;
				long millis = durationInMillis % 1000;
				long second = (durationInMillis / 1000) % 60;
				long minute = (durationInMillis / (1000 * 60)) % 60;
				long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
				prevLap.lapTime = statPoint.iLastTime;
				System.out.println(String.format("Last lap: %02d:%02d:%02d.%d", hour, minute, second, millis));
				calculateLapStats(prevLap,session); // we got new lap, calculate previous
				Gson gson = new Gson();
				System.out.println(gson.toJson(prevLap));
			}else {
				//first registered lap
				session.laps.get(statPoint.lapNo).distanceTraveled = statPoint.distanceTraveled;
				
			}
		}
		session.currentLap = lap;
		lap.lapNo = statPoint.lapNo;
		lap.distanceTraveled = statPoint.distanceTraveled;
		if (statPoint.isInPitLane == 1) {
			// we are in the pit. Did we enter the pit or started the lap from the pit?
			if (statPoint.normalizedCarPosition < 0.5)
				lap.fromPit = true;
			else
				lap.toPit = true;

			// remember fuel amount before the pit
			if (prevStatPoint != null && prevStatPoint.isInPitLane == 0) {
				fuelBeforePit = statPoint.fuel;
			}
		}

		// we left the pit. Did we add some fuel?
		if (prevStatPoint != null && prevStatPoint.isInPitLane == 1 && statPoint.isInPitLane == 0) {
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
				if (statPoint.currentMap == prevStatPoint.currentMap && statPoint.lapNo == prevStatPoint.lapNo) {
					currentMapMS += 1;
					lap.maps.put(statPoint.currentMap, currentMapMS);
				}
			}

		}
		session.sessionTimeLeft = statPoint.sessionTimeLeft;
		//calculateLapStats(lap);
	}

	private void calculateLapStats(StatLap lap, StatSession session) {
		lap.fuelUsed = lap.fuelLeftOnStart - lap.fuelLeftOnEnd;
		float minutes = (float) lap.lapTime / (1000 * 60);
		float perminutes = lap.fuelUsed / minutes;
		if (lap.lapTime > 0)
			lap.fuelAVGPerMinute = perminutes;
		float lavg = 0;
		int i = 0;
		Iterator<StatLap> i3laps = session.last3Laps.iterator();
		while (i3laps.hasNext() ) {
			i++;
			StatLap l = i3laps.next();
			lavg += l.fuelUsed;
		}
		session.fuelAVG3Laps = lavg/i;
		
		lavg = 0;
		i = 0;
		Iterator<StatLap> i5laps = session.last5Laps.iterator();
		while (i5laps.hasNext() ) {
			i++;
			StatLap l = i5laps.next();
			lavg += l.fuelUsed;
		}
		session.fuelAVG5Laps = lavg/i;
		
	}

	public void saveToXLSX() {
		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Persons");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 4000);

		Row header = sheet.createRow(0);

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);

		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Name");
		headerCell.setCellStyle(headerStyle);

		headerCell = header.createCell(1);
		headerCell.setCellValue("Age");
		headerCell.setCellStyle(headerStyle);
		// DATA
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);

		Row row = sheet.createRow(2);
		Cell cell = row.createCell(0);
		cell.setCellValue("John Smith");
		cell.setCellStyle(style);

		cell = row.createCell(1);
		cell.setCellValue(20);
		cell.setCellStyle(style);

		// SAVE
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";
		System.out.println(fileLocation);

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
}
