package ACC.saving;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import ACC.ApplicationContextAwareImpl;
import ACC.model.AC_SESSION_TYPE;
import ACC.model.PageFileStatistics;
import ACC.model.StatLap;
import ACC.model.StatSession;

@ComponentScan("ACC")
@Service
public class ACCDataSaveServiceImpl implements ACCDataSaveService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);


	@Override
	public void saveToXLS(PageFileStatistics pageFileStatistics) {
		
			Workbook workbook = new XSSFWorkbook();
			Iterator<Map.Entry<Integer, StatSession>> iterator = pageFileStatistics.sessions.entrySet().iterator();
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
				
				headerCell = header.createCell(28);
				headerCell.setCellValue("padsFL");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(29);
				headerCell.setCellValue("padsFR");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(30);
				headerCell.setCellValue("padsRL");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(31);
				headerCell.setCellValue("padsRR");
				headerCell.setCellStyle(headerStyle);
				
				headerCell = header.createCell(32);
				headerCell.setCellValue("disksFL");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(33);
				headerCell.setCellValue("disksFR");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(34);
				headerCell.setCellValue("disksRL");
				headerCell.setCellStyle(headerStyle);

				headerCell = header.createCell(35);
				headerCell.setCellValue("disksRR");
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
					
					cell = row.createCell(28);
					cell.setCellValue(df.format(lap.getValue().avgBPFL));
					cell.setCellStyle(style);
					cell = row.createCell(29);
					cell.setCellValue(df.format(lap.getValue().avgBPFR));
					cell.setCellStyle(style);
					cell = row.createCell(30);
					cell.setCellValue(df.format(lap.getValue().avgBPRL));
					cell.setCellStyle(style);
					cell = row.createCell(31);
					cell.setCellValue(df.format(lap.getValue().avgBPRR));
					cell.setCellStyle(style);
					
					cell = row.createCell(32);
					cell.setCellValue(df.format(lap.getValue().avgBDFL));
					cell.setCellStyle(style);
					cell = row.createCell(33);
					cell.setCellValue(df.format(lap.getValue().avgBDFR));
					cell.setCellStyle(style);
					cell = row.createCell(34);
					cell.setCellValue(df.format(lap.getValue().avgBDRL));
					cell.setCellStyle(style);
					cell = row.createCell(35);
					cell.setCellValue(df.format(lap.getValue().avgBDRR));
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
				pageFileStatistics.sessions.remove(key);
			});
	}

	@Override
	public void saveToGoogle(PageFileStatistics pageFileStatistics) {
		// TODO Auto-generated method stub
		
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
	
	public static String mstoStr(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d:%03d",hour, minute, second, millis);
	}

}
