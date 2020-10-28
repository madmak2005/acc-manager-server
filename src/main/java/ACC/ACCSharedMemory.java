package ACC;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;
import ACC.model.SPageFileStatic;

public class ACCSharedMemory{
	private final MyKernel32 myKernel32;
	private HANDLE h;
	private Pointer view;
	SPageFileStatic sPageFileStatic = new SPageFileStatic();
	
	public interface MyKernel32 extends Kernel32 {
	    MyKernel32 INSTANCE = (MyKernel32)Native.load("kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS);
	    HANDLE OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName);
	}
	
	public ACCSharedMemory() {
		myKernel32 = MyKernel32.INSTANCE;
		h = myKernel32.OpenFileMapping(0x4, true, "Local\\acpmf_static");
		view = Kernel32.INSTANCE.MapViewOfFile (h, 0x4, 0, 0, 688);
	}

	public SPageFileStatic getSPageFileStatic() {

		sPageFileStatic.smVersion = view.getWideString(0);
		sPageFileStatic.acVersion = view.getWideString(30);
		sPageFileStatic.numberOfSessions = view.getInt(60);
		sPageFileStatic.carModel = view.getWideString(68);
		sPageFileStatic.track = view.getWideString(134);
		sPageFileStatic.isOnline = view.getInt(684);

		return sPageFileStatic;
	}
	
	@Override
	protected void finalize() throws Throwable {
		Kernel32.INSTANCE.UnmapViewOfFile(h.getPointer());
		myKernel32.CloseHandle(h);
	}
	
}