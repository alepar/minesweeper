package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

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

    private static int[] getRect(String windowName) throws NativeException {
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

    public WinmineApplication() {
        exec(unpackWinmine());
        while(findWinmineWindow() == null) { sleep(); }
        windowDescriptor = findWinmineWindow();
    }

    public void close() throws NativeException {
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

    public static void main(String[] args) throws Exception {
        int[] rect = getRect(WINMINE_WINDOW_CAPTION);
        System.out.printf("The corner locations for the window \"%s\" are %s", WINMINE_WINDOW_CAPTION, Arrays.toString(rect));
        Robot robot = new Robot();
        BufferedImage bi = robot.createScreenCapture(new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]));
        ImageIO.write(bi, "png", new File("d:/minesweeper.png"));
    }

}
