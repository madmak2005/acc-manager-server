package ACC.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import app.Application;

public class PageFileStatistics implements Page{


	public PageFileStatistics() {
		super();
		setPageName("statistics");
	}

	private List<StatPoint> statPoints = new ArrayList<>();
	public Map<Integer,StatSession> sessions = new HashMap<>();
	private float fuelBeforePit = 0;
	private float fuelAfterPit = 0;
	private String pageName;
	
	public void addStatPoint(StatPoint statPoint) {
		StatPoint prevStatPoint = null;
		if (statPoints.size() > 0)
			prevStatPoint = statPoints.get(statPoints.size() - 1);
		
		statPoints.add(statPoint);
		StatSession session = sessions.get(statPoint.sessionIndex);
		if (session == null) {
			session = new StatSession();
			sessions.put(statPoint.sessionIndex, session);
		}
		session.car = statPoint.car;
		
		StatLap lap = session.laps.get(statPoint.lapNo);
		if (lap == null) {
			//init new lap, we don't have it in current session
			lap = new StatLap();
			session.currentLap = lap;
			lap.fuelLeftOnStart = statPoint.fuel;
			lap.fuelLeftOnEnd = statPoint.fuel; //reset in case of refueling;
			lap.maps.put(statPoint.currentMap, 0);
			StatLap prevLap = session.laps.get(statPoint.lapNo - 1);
			if (prevLap != null) {
				session.last3Laps.add(prevLap);
				session.last5Laps.add(prevLap);
				session.bestLap = session.bestLap.lapTime < prevLap.lapTime ? session.bestLap : prevLap;
				session.laps.put(statPoint.sessionIndex, prevLap);
				calculateLapStats(prevLap); //we got new lap, calculate previous
			}
		}
		
		lap.distanceTraveled = statPoint.distanceTraveled;
		if ( statPoint.isInPitLane == 1) {
			//we are in the pit. Did we enter the pit or started the lap from the pit?
			if (statPoint.normalizedCarPosition < 0.5 ) 
				lap.fromPit = true;
			else 
				lap.toPit = true;
			
			//remember fuel amount before the pit
			if (prevStatPoint != null && prevStatPoint.isInPitLane == 0 ) {
				fuelBeforePit = statPoint.fuel;
			}
		}
		
		//we left the pit. Did we add some fuel?	
		if (prevStatPoint != null && prevStatPoint.isInPitLane == 1 && statPoint.isInPitLane == 0 ) {
			fuelAfterPit = statPoint.fuel;
			if (fuelAfterPit > fuelBeforePit)
				lap.fuelAdded = fuelAfterPit - fuelBeforePit;
		}

		lap.fuelLeftOnEnd = Math.min(statPoint.fuel, lap.fuelLeftOnEnd); //keep the lowest value in case of refueling;
		lap.lapTime = statPoint.iCurrentTime;
		lap.isValidLap = !(statPoint.isValidLap == 0); //zero == false
		
		if (prevStatPoint != null) {
				//sum usage of a map during lap in milliseconds
				int currentMapMS = lap.maps.get(statPoint.currentMap);
				if (statPoint.currentMap == prevStatPoint.currentMap && statPoint.lapNo == prevStatPoint.lapNo)
					currentMapMS += statPoint.iCurrentTime - prevStatPoint.iCurrentTime;
				lap.maps.put(statPoint.currentMap, currentMapMS );
				
		}
		session.sessionTimeLeft = statPoint.sessionTimeLeft;
		calculateLapStats(lap);
	}
	
	private void calculateLapStats(StatLap lap) {
		lap.fuelUsed = lap.fuelLeftOnStart - lap.fuelLeftOnEnd;
		lap.fuelAVGPerMinute = lap.fuelUsed / (lap.lapTime * 1000 / 60);
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
		//DATA
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);

		Row row = sheet.createRow(2);
		Cell cell = row.createCell(0);
		cell.setCellValue("John Smith");
		cell.setCellStyle(style);

		cell = row.createCell(1);
		cell.setCellValue(20);
		cell.setCellStyle(style);
		
		//SAVE
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
			FilterProvider filters = new SimpleFilterProvider()  
				      .addFilter("filter1",   
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
			 FilterProvider filters = new SimpleFilterProvider()  
				      .addFilter("filter1",   
				          SimpleBeanPropertyFilter.filterOutAllExcept(fieldsFilter));
			ObjectMapper mapper = new ObjectMapper().setFilterProvider(filters);
			response = mapper.writeValueAsString(page);
		} catch (JsonProcessingException e) {
			Application.LOGGER.debug(e.toString());
		}
		return response;
	}
}
