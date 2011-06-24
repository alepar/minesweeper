package ru.alepar.minesweeper.tihirdparty;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.Arrays;

public class WindowsMinesweeperApplication implements GameApplication {

    private static final User32 USER32 = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
    private static final String WINMINE_WINDOW_CAPTION = "Minesweeper";

    public interface User32 extends StdCallLibrary {
        HWND FindWindow(String lpClassName, String lpWindowName);
        int GetWindowRect(HWND handle, int[] rect);
    }

    public static int[] getRect(String windowName) throws NativeException {
        HWND hwnd = USER32.FindWindow(null, windowName);
        if (hwnd == null) {
            throw new NativeException("couldnot find window named: " + windowName);
        }

        int[] rect = {0, 0, 0, 0};
        int result = USER32.GetWindowRect(hwnd, rect);
        if (result == 0) {
            throw new NativeException("failed to get coordinates for window named: " + windowName);
        }
        return rect;
    }

    public static void main(String[] args) throws Exception {
        int[] rect = getRect(WINMINE_WINDOW_CAPTION);
        System.out.printf("The corner locations for the window \"%s\" are %s", WINMINE_WINDOW_CAPTION, Arrays.toString(rect));
    }

}
