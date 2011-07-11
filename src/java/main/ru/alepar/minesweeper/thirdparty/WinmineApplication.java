package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class WinmineApplication {

    private static final User32 USER32 = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
    private static final int WM_CLOSE = 0x0010;
    private static final String WINMINE_WINDOW_CAPTION = "Minesweeper";

    private HWND windowDescriptor;
    private final Robot robot;

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
        assertThereAreNoMinesweepersRunning();
        exec(unpackWinmine());
        while(findWinmineWindow() == null) { safeSleep(50l); }
        windowDescriptor = findWinmineWindow();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("failed to create Robot - running headless?", e);
        }
        safeSleep(500l); //allow winmine to settle and draw itself
    }

    public void leftClickAt(Coords coords) {
        clickMouseWith(coords, InputEvent.BUTTON1_MASK);
    }

    public void rightClickAt(Coords coords) {
        clickMouseWith(coords, InputEvent.BUTTON3_MASK);
    }

    private void clickMouseWith(Coords coords, int buttonMask) {
        try {
            int[] rect = getRect();
            robot.mouseMove(coords.x + rect[0], coords.y + rect[1]);
            robot.mousePress(buttonMask);
            robot.mouseRelease(buttonMask);
        } catch (NativeException e) {
            throw new RuntimeException("failed to left click", e);
        }
    }

    private void assertThereAreNoMinesweepersRunning() {
        if (findWinmineWindow() != null) {
            throw new RuntimeException("another minesweeper is already running");
        }
    }

    public BufferedImage getScreenshot() throws NativeException {
        int[] rect = getRect();
        return robot.createScreenCapture(new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]));
    }

    public void close() {
        USER32.SendMessageA(windowDescriptor, WM_CLOSE, 0, 0);
        while(findWinmineWindow() != null) { safeSleep(50l); }
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

    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  //propagate interrupt further
        }
    }

}
