package ACC.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import app.Application;

public class OutputMessage {

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
		saveText(page.getPageName(), page.toJSON());
		this.page = page;
		this.fields = fields;
		if (fields == null || fields.size() == 0)
			this.content = page.toJSON();
		else
			this.content = page.toJSON(fields);
	}

	public OutputMessage(String content) {
		super();
		this.content = content;
	}

	public String content;
	public Page page;
	public List<String> fields;

	private void saveText(String pageName, String message) {
		if (Application.debug) {
			try {
				String pattern = "yyyy_MM_dd_HH_mm";
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
				;
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
					Compress compress7zip = new Compress(lastMinuteFile.getName());
					Thread t = new Thread(compress7zip);
					t.start();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			
			try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(new File(debugFolder.getAbsolutePath()+ "\\" +filename + ".7z"))) {
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