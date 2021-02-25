package ACC.sharedmemory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;

import ACC.model.PageFileGraphics;
import ACC.model.PageFilePhysics;
import ACC.model.PageFileStatic;
import ACC.model.PageFileStatistics;
import ACC.model.SPageFileGraphics;
import ACC.model.SPageFilePhysics;
import ACC.model.SPageFileStatic;

import app.Application;
import me.tongfei.progressbar.ProgressBar;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

import java.io.IOException;

public class ACCSharedMemory {

	public enum ACCESS_TYPE {
		PAGE_NOACCESS(0x1), PAGE_READONLY(0x2), PAGE_READWRITE(0x4), PAGE_WRITECOPY(0x8), PAGE_EXECUTE(0x10);

		int value;

		ACCESS_TYPE(int i) {
			this.value = i;
		}

	};

	private final MyKernel32 myKernel32;
	private HANDLE hStatic, hPhysics, hGraphics;
	private Pointer dStatic, dPhysics, dGraphics;
	private static final Logger LOGGER = LoggerFactory.getLogger(ACCSharedMemory.class);

	List<PageFileGraphics> pageFileGraphicsList = new ArrayList<PageFileGraphics>();
	List<PageFilePhysics> pageFilePhysicsList = new ArrayList<PageFilePhysics>();
	List<PageFileStatic> pageFileStaticList = new ArrayList<PageFileStatic>();
	Iterator<PageFileGraphics> pageFileGraphicsIterator;
	Iterator<PageFilePhysics> pageFilePhysicsIterator;
	Iterator<PageFileStatic> pageFileStaticIterator;
	final int seconds = 5;
	Instant startS = Instant.now();
	Instant startP = Instant.now();
	Instant startG = Instant.now();

	public interface MyKernel32 extends Kernel32 {
		MyKernel32 INSTANCE = Native.load("kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS);

		HANDLE OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName);
	}

	private void initStatic() {
		hStatic = myKernel32.OpenFileMapping(ACCESS_TYPE.PAGE_READWRITE.value, true, "Local\\acpmf_static");
		dStatic = Kernel32.INSTANCE.MapViewOfFile(hStatic, 0x4, 0, 0, 820);
	}

	private void initGraphics() {
		hGraphics = myKernel32.OpenFileMapping(ACCESS_TYPE.PAGE_READWRITE.value, true, "Local\\acpmf_graphics");
		dGraphics = Kernel32.INSTANCE.MapViewOfFile(hGraphics, 0x4, 0, 0, 1548);
	}

	private void initPhysics() {
		hPhysics = myKernel32.OpenFileMapping(ACCESS_TYPE.PAGE_READWRITE.value, true, "Local\\acpmf_physics");
		dPhysics = Kernel32.INSTANCE.MapViewOfFile(hPhysics, 0x4, 0, 0, 800);
	}

	public ACCSharedMemory() {
		myKernel32 = MyKernel32.INSTANCE;
		initStatic();
		initGraphics();
		initPhysics();

		if (Application.useDebug) {
			decompress();
			pageFileGraphicsIterator = pageFileGraphicsList.iterator();
			pageFilePhysicsIterator = pageFilePhysicsList.iterator();
			pageFileStaticIterator = pageFileStaticList.iterator();
		}
	}

	public PageFileStatic getPageFileStatic() {
		if (Application.useDebug && pageFileStaticIterator.hasNext()) {
			PageFileStatic ps = pageFileStaticIterator.next();
			if (!pageFileStaticIterator.hasNext()) {
				LOGGER.info("here we go again (Static)");
				pageFileStaticIterator = pageFileStaticList.iterator();
			}
			ps.setPageName("static");
			return ps;
		} else {
			System.setProperty("jna.encoding", Charset.defaultCharset().name());
			SPageFileStatic sPageFileStatic = new SPageFileStatic(dStatic);
			PageFileStatic staticPage = new PageFileStatic(sPageFileStatic);
			if (staticPage.acVersion.equals("") && Duration.between(startS, Instant.now()).getSeconds() > seconds) {
				LOGGER.info("Is ACC running? Trying to open static file again");
				initStatic();
				startS = Instant.now();
			}
			return staticPage;
		}
	}

	public PageFilePhysics getPageFilePhysics() {
		if (Application.useDebug && pageFilePhysicsIterator.hasNext()) {
			PageFilePhysics pp = pageFilePhysicsIterator.next();
			if (!pageFilePhysicsIterator.hasNext()) {
				LOGGER.info("here we go again (Physics)");
				pageFilePhysicsIterator = pageFilePhysicsList.iterator();
			}
			pp.setPageName("physics");
			return pp;
		} else {
			System.setProperty("jna.encoding", Charset.defaultCharset().name());
			SPageFilePhysics sPageFilePhysics = new SPageFilePhysics(dPhysics);
			PageFilePhysics physicsPage = new PageFilePhysics(sPageFilePhysics);
			if (physicsPage.packetId == 0 && Duration.between(startP, Instant.now()).getSeconds() > seconds) {
				LOGGER.info("Is ACC running? Trying to open physics file again");
				initPhysics();
				startP = Instant.now();
			}
			return physicsPage;
		}

	}

	public PageFileGraphics getPageFileGraphics() {
		if (Application.useDebug && pageFileGraphicsIterator.hasNext()) {
			PageFileGraphics pg;
			pg = pageFileGraphicsIterator.next();
			if (!pageFileGraphicsIterator.hasNext()) {
				LOGGER.info("here we go again (Graphics)");
				pageFileGraphicsIterator = pageFileGraphicsList.iterator();
			}
			pg.setPageName("graphics");
			return pg;
		} else {
			System.setProperty("jna.encoding", Charset.defaultCharset().name());
			SPageFileGraphics sPageFileGraphics = new SPageFileGraphics(dGraphics);
			PageFileGraphics graphicsPage = new PageFileGraphics(sPageFileGraphics);
			if (graphicsPage.packetId == 0 && Duration.between(startG, Instant.now()).getSeconds() > seconds) {
				LOGGER.info("Is ACC running? Trying to open graphics file again");
				initGraphics();
				startG = Instant.now();
			}
			return graphicsPage;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		Kernel32.INSTANCE.UnmapViewOfFile(hStatic.getPointer());
		myKernel32.CloseHandle(hStatic);
		Kernel32.INSTANCE.UnmapViewOfFile(hPhysics.getPointer());
		myKernel32.CloseHandle(hPhysics);
		Kernel32.INSTANCE.UnmapViewOfFile(hGraphics.getPointer());
		myKernel32.CloseHandle(hGraphics);
	}

	public void decompress() {
		File debugFolder = new File("debug");

		System.out.println("decompression starts");
		if (debugFolder.exists()) {
			FileFilter fileFilter = file -> !file.isDirectory() && file.getName().endsWith(".7z");

			File[] fileList = debugFolder.listFiles(fileFilter);
			Arrays.sort(fileList);
			System.out.println("files in debug folder: " + fileList.length);
			try (ProgressBar pb = new ProgressBar("Log processing", fileList.length)) {
				Gson gson = new Gson();
				for (int i = 0; i < fileList.length; i++) {
					SevenZFile sevenZFile;
					pb.step();
					try {
						sevenZFile = new SevenZFile(fileList[i]);

						SevenZArchiveEntry entry;
						while ((entry = sevenZFile.getNextEntry()) != null) {
							if (entry.isDirectory()) {
								continue;
							}

							byte[] content = new byte[(int) entry.getSize()];
							sevenZFile.read(content, 0, content.length);
							String s = new String(content, StandardCharsets.UTF_8);
							String lines[] = s.split("\\r?\\n");

							if (entry.getName().contains("graphics")) {
								for (int j = 0; j < lines.length; j++) {
									PageFileGraphics p = gson.fromJson(lines[j], PageFileGraphics.class);
									pageFileGraphicsList.add(p);
								}
							}

							if (entry.getName().contains("physics")) {
								for (int j = 0; j < lines.length; j++) {
									PageFilePhysics p = gson.fromJson(lines[j], PageFilePhysics.class);
									pageFilePhysicsList.add(p);
								}
							}

							if (entry.getName().contains("static")) {
								for (int j = 0; j < lines.length; j++) {
									PageFileStatic p = gson.fromJson(lines[j], PageFileStatic.class);
									pageFileStaticList.add(p);
								}
							}

						}
						sevenZFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("decompression ends");
		System.out.println("PageFileGraphics stat points: " + pageFileGraphicsList.size());
		System.out.println("PageFilePhysics stat points: " + pageFilePhysicsList.size());
		System.out.println("PageFileStatic stat points: " + pageFileStaticList.size());
	}

}