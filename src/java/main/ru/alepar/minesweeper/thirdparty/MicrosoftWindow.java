package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import static ru.alepar.minesweeper.thirdparty.WinmineApplication.safeSleep;

public class MicrosoftWindow implements Window {

    private static final int WM_CLOSE = 0x0010;

    private final User32 user32;
    private final HWND windowDescriptor;
    private final Robot robot;

    public MicrosoftWindow(User32 user32, HWND windowDescriptor) {
        this.user32 = user32;
        this.windowDescriptor = windowDescriptor;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("failed to create Robot - running headless?", e);
        }
    }

    private int[] getRect() throws NativeException {
        int[] rect = {0, 0, 0, 0};
        int result = user32.GetWindowRect(windowDescriptor, rect);
        if (result == 0) {
            throw new NativeException("failed to get coordinates");
        }
        return rect;
    }

    @Override
    public void leftClickAt(Coords coords) {
        clickMouseWith(coords, InputEvent.BUTTON1_MASK);
    }

    @Override
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

    @Override
    public BufferedImage getScreenshot() throws NativeException {
        int[] rect = getRect();
        return robot.createScreenCapture(new Rectangle(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]));
    }

    @Override
    public void close() {
        user32.SendMessageA(windowDescriptor, WM_CLOSE, 0, 0);
        while(user32.IsWindow(windowDescriptor)) { safeSleep(50l); }
    }

}
