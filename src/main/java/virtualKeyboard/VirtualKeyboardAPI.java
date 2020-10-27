package virtualKeyboard;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

import virtualKeyboard.model.KeyboardKey;

public class VirtualKeyboardAPI {

    private final User32 user32;

    interface User32 extends StdCallLibrary {

        User32 INSTANCE = (User32) Native.load("user32", User32.class);
        int WM_GETTEXT = 0x000D;
        int WM_GETTEXTLENGTH = 0x000E;

        WinDef.HWND FindWindowA(String lpClassName, String lpWindowName);

        WinDef.HWND FindWindowExA(WinDef.HWND hwndParent, WinDef.HWND hwndChildAfter, String lpClassName, String lpWindowName);

        WinDef.LRESULT SendMessageA(WinDef.HWND editHwnd, int wmGettext, long l, byte[] lParamStr);

        WinDef.DWORD SendInput(WinDef.DWORD dWord, WinUser.INPUT[] input, int cbSize);

    }

    public VirtualKeyboardAPI() {
        user32 = User32.INSTANCE;
    }


    public void sendText(KeyboardKey key, int downOrUp){

        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinUser.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType(WinUser.KEYBDINPUT.class); // Because setting INPUT_INPUT_KEYBOARD is not enough: https://groups.google.com/d/msg/jna-users/NDBGwC1VZbU/cjYCQ1CjBwAJ
        input.input.ki.wScan = new WinDef.WORD( 0 );
        input.input.ki.time = new WinDef.DWORD( 0 );
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR( 0 );

        input.input.ki.wVk = key.getVirtualKeyCode(); 
        input.input.ki.dwFlags = new WinDef.DWORD( downOrUp );
        user32.SendInput( new WinDef.DWORD( 1 ), ( WinUser.INPUT[] ) input.toArray( 1 ), input.size() );

    }

}

