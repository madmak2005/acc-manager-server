package ACC.saving;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import ACC.ApplicationPropertyService;
import ACC.model.AC_SESSION_TYPE;
import ACC.model.PageFileStatistics;
import ACC.model.StatLap;
import ACC.model.StatSession;
import lombok.Data;

@ComponentScan("ACC")
@Service
public class ACCDataSaveServiceImpl implements ACCDataSaveService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);

	@Autowired
	private ApplicationPropertyService applicationPropertyService;

	/*
	 * private ApplicationPropertyService applicationPropertyService =
	 * (ApplicationPropertyService) ApplicationContextAwareImpl
	 * .getApplicationContext().getBean("applicationPropertyService");
	 */
	@Override
	public String saveToXLS(PageFileStatistics pageFileStatistics) {
		List<StatSession> sessions = applicationPropertyService.getMobileSessionList();
		StatSession enduSession = applicationPropertyService.getEnduSession();
		Workbook workbook = new XSSFWorkbook();
		// Iterator<Map.Entry<Integer, StatSession>> iterator =
		// pageFileStatistics.sessions.entrySet().iterator();
		// DecimalFormat df = new DecimalFormat("0.000");
		// List<Integer> sessionsToRemove = new ArrayList<>();
		boolean write = false;
		if (sessions != null)
			for (Entry<Integer, StatSession> session : pageFileStatistics.sessions.entrySet()) {
				if (session.getValue().laps != null && session.getValue().laps.size() > 0) {
					sessionToSheet(workbook, session.getValue(), "own");
					write = true;
				}
			}
		if (enduSession != null && enduSession.laps.size() > 0) {
			sessionToSheet(workbook, enduSession, "endu");
			write = true;
		}
		if (write) {

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
			return fileLocation;
		}
		return "no data to write";

	}
	
	private void sessionToSheet(Workbook workbook, StatSession session, String tabName) {
		int rowNo = 0;
		// Map.Entry<Integer, StatSession> entry = iterator.next();
		// if (iterator.hasNext())
		// sessionsToRemove.add(entry.getKey());
		if (session.laps != null) {
			int lastLapNo = 0;
			Sheet sheet = workbook.createSheet(tabName + " " + session.internalSessionIndex);
			sheet.setColumnWidth(0, 5000);
			sheet.setColumnWidth(1, 6000);
			sheet.setColumnWidth(2, 6000);
			sheet.setColumnWidth(3, 3000);
			sheet.setColumnWidth(4, 2000);
			sheet.setColumnWidth(5, 3000);
			sheet.setColumnWidth(6, 3000);
			sheet.setColumnWidth(7, 3000);
			sheet.setColumnWidth(8, 3000);
			sheet.setColumnWidth(25, 3000);
			sheet.setColumnWidth(30, 3000);
			sheet.setColumnWidth(31, 3000);

			Row header = sheet.createRow(rowNo++);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			font.setFontName("Arial");
			font.setFontHeightInPoints((short) 12);
			font.setBold(true);
			headerStyle.setFont(font);

			/*
			 * Cell headerCell = header.createCell(0); headerCell.setCellValue("Track");
			 * headerCell.setCellStyle(headerStyle);
			 * 
			 * headerCell = header.createCell(1); headerCell.setCellValue("Car");
			 * headerCell.setCellStyle(headerStyle);
			 * 
			 * headerCell = header.createCell(2); headerCell.setCellValue("Driver name");
			 * headerCell.setCellStyle(headerStyle);
			 * 
			 * headerCell = header.createCell(3); headerCell.setCellValue("Session type");
			 * headerCell.setCellStyle(headerStyle);
			 * 
			 * // DATA CellStyle style = workbook.createCellStyle();
			 * style.setWrapText(true);
			 * 
			 * Row row = sheet.createRow(rowNo++); Cell cell = row.createCell(0);
			 * cell.setCellValue(session.car.track); cell.setCellStyle(style);
			 * 
			 * cell = row.createCell(1); cell.setCellValue(session.car.carModel);
			 * cell.setCellStyle(style);
			 * 
			 * cell = row.createCell(2); cell.setCellValue(session.car.playerName);
			 * cell.setCellStyle(style);
			 * 
			 * cell = row.createCell(3); switch (session.getSession_TYPE()) { case
			 * AC_SESSION_TYPE.AC_QUALIFY: cell.setCellValue("QUALIFY"); break; case
			 * AC_SESSION_TYPE.AC_PRACTICE: cell.setCellValue("PRACTICE"); break; case
			 * AC_SESSION_TYPE.AC_RACE: cell.setCellValue("RACE"); break; case
			 * AC_SESSION_TYPE.AC_HOTLAP: cell.setCellValue("HOTLAP"); break; }
			 * cell.setCellStyle(style);
			 * 
			 * row = sheet.createRow(rowNo++); header = sheet.createRow(rowNo++); header =
			 * sheet.createRow(rowNo++);
			 * 
			 * headerCell = header.createCell(0); headerCell.setCellValue("Recorded Laps");
			 * headerCell.setCellStyle(headerStyle);
			 * 
			 * row = sheet.createRow(rowNo++);
			 */
			CellStyle style = workbook.createCellStyle();
			style.setWrapText(true);

			// Cell headerCell = header.createCell(0);
			// Row row = sheet.createRow(0);
			// Cell cell = row.createCell(0);

			int headerNo = 0;
			Cell headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("driver name");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("cCar");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("track");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("session");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("lapNo");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("lapTime");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("splitTime 1");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("splitTime 2");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("splitTime 3");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("Fuel start lap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("Fuel end lap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("l/min");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("l/lap [ACC]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("l/lap [AVG5]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("refuel [l]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("pFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("pFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("pRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("pRR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("tFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("tFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("tRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("tRR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("rainIntensity");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("rainTyres");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("trackGripStatus");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("trackStatus");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("validLap");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("airTemp");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("roadTemp");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("Fuel for next [lap]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("Fuel for next [time]");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("Seesion time left");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("padsFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("padsFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("padsRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("padsRR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("disksFL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("disksFR");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("disksRL");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("disksRR");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("strategyTyreSet");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("currentTyreSet");
			headerCell.setCellStyle(headerStyle);

			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("position");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("driverStintTotalTimeLeft");
			headerCell.setCellStyle(headerStyle);
			
			headerCell = header.createCell(headerNo++);
			headerCell.setCellValue("driverStintTimeLeft");
			headerCell.setCellStyle(headerStyle);
			
			Iterator<Map.Entry<Integer, StatLap>> iteratorLap = session.laps.entrySet().iterator();
			int i = 0;

			while (iteratorLap.hasNext()) {

				int cellNo = 0;
				Map.Entry<Integer, StatLap> lap = iteratorLap.next();
				if (lastLapNo != lap.getValue().lapNo) {
					lastLapNo = lap.getValue().lapNo;
					Row row = sheet.createRow(rowNo++);

					Cell cell = row.createCell(cellNo++);
					
					cell.setCellValue(lap.getValue().driverName);
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().carModel);
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().track);
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					switch (session.getSession_TYPE()) {
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

					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().lapNo);
					cell.setCellStyle(style);

					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(mstoStr(lap.getValue().lapTime));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					if (lap.getValue().splitTimes.get(1) != null) {
						cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(1)));
						cell.setCellStyle(style);
					}

					cell = row.createCell(cellNo++);
					if (lap.getValue().splitTimes.get(2) != null) {
						cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(2) - lap.getValue().splitTimes.get(1)));
						cell.setCellStyle(style);
					}

					cell = row.createCell(cellNo++);
					if (lap.getValue().splitTimes.get(3) != null) {

						cell.setCellValue(mstoStr(lap.getValue().splitTimes.get(3) - lap.getValue().splitTimes.get(2)));
						cell.setCellStyle(style);
					}
					DecimalFormat df = new DecimalFormat("0.000");
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelLeftOnStart));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelLeftOnEnd));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelAVGPerMinute));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelXlap));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelAVGPerLap));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelAdded));
					cell.setCellStyle(style);

					boolean wet = lap.getValue().rainTyres == 1 ? true : false;

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgpFL));
					cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpFL, wet));
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgpFR));
					cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpFR, wet));
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgpRL));
					cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpRL, wet));
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgpRR));
					cell.setCellStyle(pressureXLSXStyle(workbook, lap.getValue().avgpRR, wet));

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgtFL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgtFR));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgtRL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgtRR));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgRainIntensity));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().rainTyres);
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().avgTrackGripStatus);
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().trackStatus);
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(lap.getValue().isValidLap);
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgAirTemp));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgRoadTemp));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().fuelEFNLapsOnEnd));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(mstoStr(Math.round(lap.getValue().fuelEstForNextMiliseconds)));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(mstoStr(Math.round(lap.getValue().sessionTimeLeft)));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBPFL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBPFR));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBPRL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBPRR));
					cell.setCellStyle(style);

					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBDFL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBDFR));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBDRL));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().avgBDRR));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().strategyTyreSet));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().currentTyreSet));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().position));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().driverStintTotalTimeLeft));
					cell.setCellStyle(style);
					cell = row.createCell(cellNo++);
					cell.setCellValue(df.format(lap.getValue().driverStintTimeLeft));
					cell.setCellStyle(style);
				}
			}
		}
	}

	@Override
	public synchronized boolean saveToGoogle(PageFileStatistics pageFileStatistics) {
		
		String spreadsheetId = applicationPropertyService.getSheetID();
		if (spreadsheetId != null && !spreadsheetId.isEmpty()) {
			try {
				final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				String range = "Class Data!A2:E";
				Credential credential;

				credential = GoogleController.flow.loadCredential(GoogleController.USER_IDENTIFIER_KEY);

				Sheets service = new Sheets.Builder(HTTP_TRANSPORT, GoogleController.JSON_FACTORY, credential)
						.setApplicationName(GoogleController.APPLICATION_NAME).build();

				TabInfo tabInfo = getTab(service, pageFileStatistics);
				String tabName = tabInfo.tabName;
				pageFileStatistics.googleSaved = tabInfo.googleSaved;
				
				//saveHeaderToGoogle(pageFileStatistics);
				
				Iterator<Map.Entry<Integer, StatSession>> iterator = pageFileStatistics.sessions.entrySet().iterator();
				
				List<List<Object>> values = new ArrayList<>();
				while (iterator.hasNext()) {

					Map.Entry<Integer, StatSession> entry = iterator.next();

					Iterator<Map.Entry<Integer, StatLap>> iteratorLap = entry.getValue().laps.entrySet().iterator();
					int ii = 1;
					
					if (!pageFileStatistics.googleSaved) {
						List<Object> header = Arrays.asList("internalLapIndex", "Lap time", "Split 1", "Split 2", "Split 3",
								"Fuel [start line]", "Fuel [finish line]", "Fuel afg [l/minute]", "Fuel avg [l/lap]",
								"Refuel [l]", "Used wet", "PSI FL", "PSI FR", "PSI RL", "PSI RR", "°C FL", "°C FR", "°C RL",
								"°C RR", "Rain intensity", "Track grip status", 
								"Air [°C]", "Road [°C]", "Est for next [laps]", "Est for next [time]", "Session Time Left",
								"Worst brake pad", "Worst brake disk"
						);
						values.add(header);
					}
					range = tabName + "!A" + 7 + ":AL";
					ValueRange body = new ValueRange().setValues(values);
					UpdateValuesResponse result = service.spreadsheets().values().update(spreadsheetId, range, body)
							.setValueInputOption("RAW").execute();
					values.clear();
					
					boolean firstLapToSave = true;
					while (iteratorLap.hasNext()) {
						Map.Entry<Integer, StatLap> lap = iteratorLap.next();
							if(!lap.getValue().saved) {
								if (firstLapToSave || !pageFileStatistics.googleSaved) {
									ii = lap.getValue().internalLapIndex;
									firstLapToSave = false;
								}
								LOGGER.info(lap.getValue().splitTimes.toString());
								List<Object> v = Arrays.asList(
										lap.getValue().internalLapIndex,
										mstoStr(lap.getValue().lapTime),
										lap.getValue().splitTimes.get(1) != null ? mstoStr(lap.getValue().splitTimes.get(1))
												: 0,
										lap.getValue().splitTimes.get(2) != null && lap.getValue().splitTimes.get(1) != null
												? mstoStr(lap.getValue().splitTimes.get(2) - lap.getValue().splitTimes.get(1))
												: 0,
										lap.getValue().splitTimes.get(3) != null
												? mstoStr(lap.getValue().splitTimes.get(3) - lap.getValue().splitTimes.get(2))
												: 0,
										new BigDecimal(lap.getValue().fuelLeftOnStart).setScale(3, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().fuelLeftOnEnd).setScale(3, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().fuelAVGPerMinute).setScale(3, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().fuelXlap).setScale(3, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().fuelAdded).setScale(0, RoundingMode.HALF_UP).doubleValue(), 
										lap.getValue().rainTyres == 1 ? true : false,
										new BigDecimal(lap.getValue().avgpFL).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgpFR).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().avgpRL).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgpRR).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().avgtFL).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgtFR).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().avgtRL).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgtRR).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().avgRainIntensity).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgTrackGripStatus).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().avgAirTemp).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(lap.getValue().avgRoadTemp).setScale(2, RoundingMode.HALF_UP).doubleValue(), 
										new BigDecimal(lap.getValue().fuelEFNLapsOnEnd).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										mstoStr(Math.round(lap.getValue().fuelEstForNextMiliseconds)),
										mstoStr(Math.round(lap.getValue().sessionTimeLeft)), 
										new BigDecimal(Math.min( Math.min(lap.getValue().avgBPFL, lap.getValue().avgBPFR), Math.min(lap.getValue().avgBPRL, lap.getValue().avgBPRR) ) ).setScale(2, RoundingMode.HALF_UP).doubleValue(),
										new BigDecimal(Math.min( Math.min(lap.getValue().avgBDFL, lap.getValue().avgBDFR), Math.min(lap.getValue().avgBDRL, lap.getValue().avgBDRR) ) ).setScale(2, RoundingMode.HALF_UP).doubleValue()
								);
								
								values.add(v);
								
							}
						if (values.size() > 1) {
							for (List<Object> o : values) {
								List<List<Object>> lo = new ArrayList<>();
								lo.add(o);
								body = new ValueRange().setValues(lo);
								int lapLoc = ii + 8;
								range = tabName + "!A" + lapLoc + ":AL";
								LOGGER.info("Values size: " + lo.size());
								if (lo.size()>0) {
									LOGGER.info("UPDATE SHEET");
									result = service.spreadsheets().values().update(spreadsheetId, range, body)
										.setValueInputOption("RAW").execute();
									LOGGER.info("Result size: " + result.size());
									try {
										Thread.sleep(3*1000);
									} catch (InterruptedException e) {
										LOGGER.error(e.toString());
									}
								}
								lo.clear();
							}
						}else {
							body = new ValueRange().setValues(values);
							int lapLoc = ii + 8;
							range = tabName + "!A" + lapLoc + ":AL";
							LOGGER.info("Values size: " + values.size());
							if (values.size()>0) {
								LOGGER.info("UPDATE SHEET");
								result = service.spreadsheets().values().update(spreadsheetId, range, body)
									.setValueInputOption("RAW").execute();
								LOGGER.info("Result size: " + result.size());
							}
							values.clear();

						}
					}
				}

			} catch (IOException | GeneralSecurityException e) {
				LOGGER.info(e.toString());
				return false;
			}
		} else {
			//LOGGER.info("Google service not yet configured.");
			return false;
		}
		return true;
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
			if (psi < 27.5)
				return coldStyle;
			else if (psi >= 27.5 && psi <= 28.0)
				return normalStyle;
			else if (psi > 28.0 && psi < 28.5)
				return warmStyle;
			else
				return hotStyle;
		} else {
			return normalStyle;
		}
	}

	public static String mstoStr(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%03d", hour, minute, second, millis);
	}

	@Override
	public boolean saveHeaderToGoogle(PageFileStatistics pageFileStatistics) {
		String spreadsheetId = applicationPropertyService.getSheetID();
		if (spreadsheetId != null && !spreadsheetId.isEmpty()) {
			try {
				final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				String range = "Class Data!A2:E";
				Credential credential;

				credential = GoogleController.flow.loadCredential(GoogleController.USER_IDENTIFIER_KEY);

				Sheets service = new Sheets.Builder(HTTP_TRANSPORT, GoogleController.JSON_FACTORY, credential)
						.setApplicationName(GoogleController.APPLICATION_NAME).build();
				
				TabInfo tabInfo = getTab(service, pageFileStatistics);
				String tabName = tabInfo.tabName;
				pageFileStatistics.googleSaved = tabInfo.googleSaved;
				
				
				Iterator<Map.Entry<Integer, StatSession>> iterator = pageFileStatistics.sessions.entrySet().iterator();
				
				List<List<Object>> values = new ArrayList<>();
				while (iterator.hasNext()) {

					Map.Entry<Integer, StatSession> entry = iterator.next();

					StatSession session = entry.getValue();

					List<Object> sessionHeader = Arrays.asList("Car", "Track", "Current time in game");
					values.add(sessionHeader);
					int seconds = (int) Math.floor(session.currentLap.clockAtStart);
					long hours = TimeUnit.SECONDS.toHours(seconds);
					long min = TimeUnit.SECONDS.toMinutes(seconds) - 60 * hours;
					long sec = seconds - (60 * 60 * hours) - (60 * min);
					String clockAtStart = String.format("%02d:%02d:%02d", hours,min,sec
					  );
					List<Object> sessionValues = Arrays.asList(
							session.car.carModel, session.car.track, clockAtStart);					
					values.add(sessionValues);
					ValueRange body = new ValueRange().setValues(values);
					range = tabName + "!A" + 1 + ":AL";
					UpdateValuesResponse result = service.spreadsheets().values().update(spreadsheetId, range, body)
							.setValueInputOption("RAW").execute();
					values.clear();
					
					if (!pageFileStatistics.googleSaved) {
						List<Object> headerMFD = Arrays.asList(
						"mfdTyreSet",
						"mfdFuelToAdd",
						"mfdTyrePressureLF",
						"mfdTyrePressureRF",
						"mfdTyrePressureLR",
						"mfdTyrePressureRR",
						"ACC_RAIN_INTENSITY",
						"ACC_RAIN_INTENSITY",
						"currentTyreSet",
						"strategyTyreSet"
						);
						values.add(headerMFD);
						range = tabName + "!A" + 4 + ":AL";
						body = new ValueRange().setValues(values);
						result = service.spreadsheets().values().update(spreadsheetId, range, body)
								.setValueInputOption("RAW").execute();
						values.clear();
					}
					List<Object> valuesMFD = Arrays.asList(
					session.currentLap.mfdTyreSet,          
					session.currentLap.mfdFuelToAdd,      
					new BigDecimal(session.currentLap.mfdTyrePressureLF).setScale(1, RoundingMode.HALF_UP).doubleValue(),
					new BigDecimal(session.currentLap.mfdTyrePressureRF).setScale(1, RoundingMode.HALF_UP).doubleValue(),
					new BigDecimal(session.currentLap.mfdTyrePressureLR).setScale(1, RoundingMode.HALF_UP).doubleValue(),
					new BigDecimal(session.currentLap.mfdTyrePressureRR).setScale(1, RoundingMode.HALF_UP).doubleValue(),
					session.currentLap.rainIntensityIn10min,
					session.currentLap.rainIntensityIn30min,
					session.currentLap.currentTyreSet,      
					session.currentLap.strategyTyreSet);     
					values.add(valuesMFD);
					
					range = tabName + "!A" + 5 + ":AL";
					body = new ValueRange().setValues(values);
					result = service.spreadsheets().values().update(spreadsheetId, range, body)
							.setValueInputOption("RAW").execute();
					values.clear();
				}
			} catch (IOException | GeneralSecurityException e) {
				LOGGER.info(e.toString());
				return false;
			}
		} else {
			//LOGGER.info("Google service not yet configured.");
			return false;
		}
		return true;
	}
	
	private TabInfo getTab(Sheets service, PageFileStatistics pageFileStatistics) {
		TabInfo tabInfo = new TabInfo();
		String spreadsheetId = applicationPropertyService.getSheetID();
		Spreadsheet sp;
		try {
			sp = service.spreadsheets().get(spreadsheetId).execute();

		List<com.google.api.services.sheets.v4.model.Sheet> sheets = sp.getSheets();
		boolean tabForDriverExists = false;
		Iterator<com.google.api.services.sheets.v4.model.Sheet> i = sheets.iterator();
		com.google.api.services.sheets.v4.model.Sheet ourTab = null;
		String tabName=LocalDate.now().toString()+ "_" + pageFileStatistics.currentSession.internalSessionIndex + "_" + pageFileStatistics.currentSession.car.playerNick;
		tabInfo.tabName = tabName;
		
		if (i.hasNext()) {
			do {
				com.google.api.services.sheets.v4.model.Sheet sh = i.next();
				if (sh.getProperties().getTitle().equals(tabName)) {
					ourTab = sh;
					tabForDriverExists = true;
				}
			} while (i.hasNext());
		} 

		if (!tabForDriverExists) {
			sheets.add(ourTab);
			sp.setSheets(sheets);
			List<Request> requests = new ArrayList<>();
			requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(
					new SheetProperties().setTitle(tabName))));
			BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
			BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(spreadsheetId, body)
					.execute();
			System.out.println(response.toPrettyString());
			tabInfo.googleSaved = false;
		}
		
		} catch (IOException e) {
			LOGGER.info(e.toString());
		}
		
		return tabInfo;
	}
	
	@Data
	private class TabInfo{
		boolean googleSaved;
		String tabName;
	}
}

