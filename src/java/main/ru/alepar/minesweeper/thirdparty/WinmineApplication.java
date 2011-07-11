package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.platform.win32.WinDef.HWND;

public class WinmineApplication {

    private static final String WINMINE_WINDOW_CAPTION = "Minesweeper";
    private static final String WINMINE_RESOURCE = "winmine.exe";

    private final User32 user32;
    private final Window wnd;

    public WinmineApplication(User32 user32, ResourceLauncher launcher) {
        this.user32 = user32;

        assertThereAreNoMinesweepersRunning();
        launcher.launch(WINMINE_RESOURCE);

        while(findWinmineWindow() == null) { safeSleep(50l); }
        safeSleep(500l); //allow winmine to settle and draw itself
        this.wnd = new MicrosoftWindow(this.user32, findWinmineWindow());
    }

    public Window getWindow() {
        return wnd;
    }

    private void assertThereAreNoMinesweepersRunning() {
        if (findWinmineWindow() != null) {
            throw new RuntimeException("another minesweeper is already running");
        }
    }

    public HWND findWinmineWindow() {
        return user32.FindWindow(null, WINMINE_WINDOW_CAPTION);
    }

    public static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  //propagate interrupt further
        }
    }

    public void close() {
        wnd.close();
    }
}
