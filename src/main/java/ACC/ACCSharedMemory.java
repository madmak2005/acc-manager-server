package ACC;

import java.nio.charset.Charset;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;

import ACC.model.PageFileGraphics;
import ACC.model.PageFilePhysics;
import ACC.model.PageFileStatic;
import ACC.model.SPageFileGraphics;
import ACC.model.SPageFilePhysics;
import ACC.model.SPageFileStatic;

public class ACCSharedMemory{
	private final MyKernel32 myKernel32;
	private HANDLE hStatic, hPhysics, hGraphics;
	private Pointer dStatic, dPhysics, dGraphics;
	
	public interface MyKernel32 extends Kernel32 {
	    MyKernel32 INSTANCE = (MyKernel32)Native.load("kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS);
	    HANDLE OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName);
	}
	
	public ACCSharedMemory() {
		myKernel32 = MyKernel32.INSTANCE;
		hStatic = myKernel32.OpenFileMapping(0x4, true, "Local\\acpmf_static");
		dStatic = Kernel32.INSTANCE.MapViewOfFile (hStatic, 0x4, 0, 0, 688);
		
		hPhysics = myKernel32.OpenFileMapping(0x4, true, "Local\\acpmf_physics");
		dPhysics = Kernel32.INSTANCE.MapViewOfFile (hPhysics, 0x4, 0, 0, 712);
		
		hGraphics = myKernel32.OpenFileMapping(0x4, true, "Local\\acpmf_graphics");
		dGraphics = Kernel32.INSTANCE.MapViewOfFile (hGraphics, 0x4, 0, 0, 712);
	}

	public PageFileStatic getPageFileStatic() {
		System.setProperty("jna.encoding", Charset.defaultCharset().name());
		SPageFileStatic sPageFileStatic = new SPageFileStatic(dStatic);
		PageFileStatic staticPage = new PageFileStatic(sPageFileStatic);
		return staticPage;
	}
	
	public PageFilePhysics getPageFilePhysics() {
		System.setProperty("jna.encoding", Charset.defaultCharset().name());
		SPageFilePhysics sPageFilePhysics = new SPageFilePhysics(dPhysics);
		PageFilePhysics physicsPage = new PageFilePhysics(sPageFilePhysics);
		return physicsPage;
	}
	
	public PageFileGraphics getPageFileGraphics() {
		System.setProperty("jna.encoding", Charset.defaultCharset().name());
		SPageFileGraphics sPageFileGraphics = new SPageFileGraphics(dGraphics);
		PageFileGraphics graphicsPage = new PageFileGraphics(sPageFileGraphics);
		return graphicsPage;
	}
	
	@Override
	protected void finalize() throws Throwable {
		Kernel32.INSTANCE.UnmapViewOfFile(hStatic.getPointer());
		myKernel32.CloseHandle(hStatic);
		Kernel32.INSTANCE.UnmapViewOfFile(hPhysics.getPointer());
		myKernel32.CloseHandle(hPhysics);
	}
	
}