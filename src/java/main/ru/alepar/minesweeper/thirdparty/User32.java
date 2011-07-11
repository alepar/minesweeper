package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends StdCallLibrary {

    public static final User32 USER32 = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

    HWND FindWindow(String lpClassName, String lpWindowName);
    int GetWindowRect(HWND handle, int[] rect);
    long SendMessageA(HWND hWnd, int msg, int num1, int num2);
    boolean IsWindow(HWND hwnd);

}
