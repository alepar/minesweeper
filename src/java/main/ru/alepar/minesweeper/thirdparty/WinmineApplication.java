package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class WinmineApplication {

    private static final User32 USER32 = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
    private static final int WM_CLOSE = 0x0010;
    private static final String WINMINE_WINDOW_CAPTION = "Minesweeper";

    private HWND windowDescriptor;

    private interface User32 extends StdCallLibrary {

        HWND FindWindow(String lpClassName, String lpWindowName);
        int GetWindowRect(HWND handle, int[] rect);
        long SendMessageA(HWND hWnd, int msg, int num1, int num2);
    }
    private int[] getRect() throws NativeException {
        int[] rect = {0, 0, 0, 0};
        int result = USER32.GetWindowRect(windowDescriptor, rect);
        if (result == 0) {
            throw new NativeException("failed to get coordinates");
        }
        return rect;
    }

    public WinmineApplication() {
        assertThatNoMinesweepersAreRunning();
        exec(unpackWinmine());
        while(findWinmineWindow() == null) { sleep(); }
        windowDescriptor = findWinmineWindow();
    }

    private void assertThatNoMinesweepersAreRunning() {
        if (findWinmineWindow() != null) {
            throw new RuntimeException("another minesweeper is already running");
        }
    }

    public BufferedImage getScreenshot() throws NativeException {
        int[] rect = getRect();
        try {
            Robot robot = new Robot();
            return robot.createScreenCapture(new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]));
        } catch (AWTException e) {
            throw new RuntimeException("Robot failed to take screenshot", e);
        }
    }

    public void close() {
        USER32.SendMessageA(windowDescriptor, WM_CLOSE, 0, 0);
        while(findWinmineWindow() != null) { sleep(); }
    }

    private static void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException("todo");
        }
    }

    static HWND findWinmineWindow() {
        return USER32.FindWindow(null, WINMINE_WINDOW_CAPTION);
    }

    private static void exec(File winmineFile) {
        try {
            Runtime.getRuntime().exec(winmineFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("failed to startup winmine.exe");
        }
    }

    private static File unpackWinmine() {
        File winmineFile;
        try {
            InputStream is = openResource("winmine.exe");
            winmineFile = File.createTempFile("winmine", ".exe");
            OutputStream os = new FileOutputStream(winmineFile);

            copy(is, os);
        } catch (IOException e) {
            throw new RuntimeException("failed to unpack winmine.exe");
        }
        return winmineFile;
    }

    private static InputStream openResource(String name) {
        InputStream is = WinmineApplication.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new RuntimeException("resource " + name + " not found on classpath");
        }
        return is;
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        try {
            try {
                byte buf[] = new byte[102400];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

}
