package virtualKeyboard;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

import virtualKeyboard.model.KeyboardKey;

public class VirtualKeyboardAPI {
	final int DOWN = 0;
	final int UP = 2;
	private final User32 user32;

	interface User32 extends StdCallLibrary {

		User32 INSTANCE = Native.load("user32", User32.class);
		int WM_GETTEXT = 0x000D;
		int WM_GETTEXTLENGTH = 0x000E;

		WinDef.HWND FindWindowA(String lpClassName, String lpWindowName);

		WinDef.HWND FindWindowExA(WinDef.HWND hwndParent, WinDef.HWND hwndChildAfter, String lpClassName,
				String lpWindowName);

		WinDef.LRESULT SendMessageA(WinDef.HWND editHwnd, int wmGettext, long l, byte[] lParamStr);

		WinDef.DWORD SendInput(WinDef.DWORD dWord, WinUser.INPUT[] input, int cbSize);

	}

	public VirtualKeyboardAPI() {
		user32 = User32.INSTANCE;
	}

	public void sendText(KeyboardKey key, int downOrUp, int time) {

		WinUser.INPUT input = new WinUser.INPUT();
		input.type = new WinUser.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
		input.input.setType(WinUser.KEYBDINPUT.class); // Because setting INPUT_INPUT_KEYBOARD is not enough:
														// https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
		input.input.ki.wScan = new WinDef.WORD(0);
		input.input.ki.time = new WinDef.DWORD(time);
		input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

		input.input.ki.wVk = key.getVirtualKeyCode();
		input.input.ki.dwFlags = new WinDef.DWORD(downOrUp);
		user32.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

	}

	public String execute(Map<String, String> allParams) {
		ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		String key = allParams.get("key");

		if (key != null) {
			if (key.length() == 1) {
				int time = 0;
				if (allParams.get("time") != null)
					time = Integer.valueOf(allParams.get("time"));
				int t = time;
				KeyboardKey keyboardKey = KeyboardKey.getByCodename(key);
				if (Character.isUpperCase(key.charAt(0))) {
					Runnable runnableTask = () -> {
						System.out.println("isUpperCase");
						try {
							sendText(KeyboardKey.RSHIFT, DOWN, 0);
							TimeUnit.MILLISECONDS.sleep(5);
							sendText(keyboardKey, DOWN, 0);
							TimeUnit.MILLISECONDS.sleep(t);
							TimeUnit.MILLISECONDS.sleep(5);
							sendText(keyboardKey, UP, 0);
							TimeUnit.MILLISECONDS.sleep(100);
							sendText(KeyboardKey.RSHIFT, UP, 0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
					executorService.execute(runnableTask);
				} else {
					Runnable runnableTask = () -> {
						try {
							sendText(keyboardKey, DOWN, 0);
							TimeUnit.MILLISECONDS.sleep(t);
							sendText(keyboardKey, UP, 0);
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
					executorService.execute(runnableTask);
				}
			} else {
				if (KeyboardKey.getByCodename(key) != null) {
					sendText(KeyboardKey.getByCodename(key), DOWN, 0);
					sendText(KeyboardKey.getByCodename(key), UP, 0);
				}
			}
		} else {
			String string = allParams.get("string");
			if (string != null && KeyboardKey.getByCodename(string) != null) {
				if (string.contains("SHIFT_")) {
					System.out.println(string);
					sendText(KeyboardKey.RSHIFT, DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), UP, 0);
					sendText(KeyboardKey.RSHIFT, UP, 0);
				} else if (string.contains("RALT_")) {
					System.out.println(string);
					sendText(KeyboardKey.RMENU, DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), UP, 0);
					sendText(KeyboardKey.RMENU, UP, 0);
				} else if (string.contains("LALT_")) {
					System.out.println(string);
					sendText(KeyboardKey.LMENU, DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), UP, 0);
					sendText(KeyboardKey.LMENU, UP, 0);
				} else {
					System.out.println(string);
					sendText(KeyboardKey.getByCodename(string), DOWN, 0);
					sendText(KeyboardKey.getByCodename(string), UP, 0);
				}
			}
		}
		/*
		 * try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */

		return "OK";
	}

}
