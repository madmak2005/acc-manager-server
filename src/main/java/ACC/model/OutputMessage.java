package ACC.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.sparsebits.SparseBitSet.Statistics;

import app.Application;

public class OutputMessage {
	public String content = "";
	public Page page;
	public List<String> fields;
	protected long timestamp = 0;
	private String pageName = "";
	private static final Logger LOGGER = LoggerFactory.getLogger(PageFileStatistics.class);

	@SuppressWarnings("unused")
	private String getContent() {
		return content;
	}

	@SuppressWarnings("unused")
	private void setContent(String content) {
		this.content = content;
	}

	public OutputMessage(Page page, List<String> fields) {
		super();
		this.page = page;
		this.fields = fields;
		this.timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
		this.pageName = page.getPageName();
		if (Application.debug && !Application.useDebug && page != null) {
				if (!pageName.equals("statistics") && page.isACCConnected()) {
					savePage(page);
				}
		}
		if (!pageName.equals("statistics")) {
			if (fields == null || fields.size() == 0)
				this.content = page.toJSON();
			else
				this.content = page.toJSON(fields);
		} else {
			PageFileStatistics stat = (PageFileStatistics) page;
			Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues() // This is the key
                .serializeNulls()
                .create();
			//stat.currentSession.last3Laps = new CircularFifoQueue<>(3);
			//stat.currentSession.last5Laps = new CircularFifoQueue<>(5);
			//stat.currentSession.lastLap = new StatLap();
			
			this.content = gson.toJson(stat.toJSON());
		}

	}

	public OutputMessage(String content) {
		super();
		this.content = content;
	}

	private void saveText(String message) {
		try {
			String pattern = "yyyy_MM_dd_HH_mm";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			LocalDateTime now = LocalDateTime.now();
			String nowDate = now.format(formatter);

			// Writing to a file
			File file = new File(nowDate + "_" + pageName + ".json");
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.append(message + '\n');
			fileWriter.flush();
			fileWriter.close();

			LocalDateTime lastMinute = now.minusMinutes(1);
			String lastMinuteDate = lastMinute.format(formatter);

			File lastMinuteFile = new File(lastMinuteDate + "_" + pageName + ".json");
			if (lastMinuteFile.exists()) {
				if (!Application.useDebug) {
					Compress compress7zip = new Compress(lastMinuteFile.getName());
					Thread t = new Thread(compress7zip);
					t.start();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void savePage(Page page) {
		// Gson gson = new Gson();
		// LOGGER.info(page.getPageName());
		// String json = page.toJSON();
		JsonObject jsonObject = JsonParser.parseString(page.toJSON()).getAsJsonObject();
		// LOGGER.info(jsonObject.toString());
		// if (page.getPageName().equals("statistics"))
		// jsonObject.getAsJsonObject("page").remove("statPoints");
		saveText(jsonObject.toString());

	}

	class Compress implements Runnable {

		private String filename;

		public Compress(String filename) {
			this.filename = filename;
		}

		public void run() {
			File debugFolder = new File("debug");
			if (!debugFolder.exists())
				debugFolder.mkdir();

			try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(
					new File(debugFolder.getAbsolutePath() + "\\" + filename + ".7z"))) {
				File file = new File(filename);
				File fileTmp = new File("_" + filename);
				file.renameTo(fileTmp);
				if (!file.isDirectory()) {
					try (FileInputStream fis = new FileInputStream(fileTmp)) {
						SevenZArchiveEntry entry_1 = sevenZOutput.createArchiveEntry(fileTmp, fileTmp.toString());
						sevenZOutput.putArchiveEntry(entry_1);
						sevenZOutput.write(Files.readAllBytes(fileTmp.toPath()));
						sevenZOutput.closeArchiveEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				sevenZOutput.finish();
				fileTmp.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}