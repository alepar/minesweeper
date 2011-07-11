package ru.alepar.minesweeper.thirdparty;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.alepar.minesweeper.testsupport.DesignedFor;
import ru.alepar.minesweeper.testsupport.OsSpecificRespectingClassRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static ru.alepar.minesweeper.testsupport.OS.WINDOWS;


@RunWith(OsSpecificRespectingClassRunner.class)
public class WinmineApplicationTest {

    @Test
    public void dummySoThatClassRunnerDoesNotComplain() throws Exception { /*do not remove*/ }

    @Test(timeout = 2000L) @DesignedFor(WINDOWS)
    public void startsAndClosesApplicationWithoutExceptions() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            assertThat(app.findWinmineWindow(), notNullValue());
        } finally {
            app.close();
        }
        assertThat(app.findWinmineWindow(), nullValue());
    }

    @Test @DesignedFor(WINDOWS)
    public void suppliesNotNullWindowScreenshot() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            assertThat(app.getWindow().getScreenshot(), notNullValue());
        } finally {
            app.close();
        }
    }
}
