package ru.alepar.minesweeper.thirdparty;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;
import ru.alepar.minesweeper.testsupport.DesignedFor;
import ru.alepar.minesweeper.testsupport.OsSpecificRespectingClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static ru.alepar.minesweeper.testsupport.OS.WINDOWS;

@RunWith(OsSpecificRespectingClassRunner.class)
public class WinmineFieldApiTest {

    @Test
    public void dummySoThatClassRunnerDoesNotComplain() throws Exception { /*do not remove*/ }

    @Test @DesignedFor(WINDOWS)
    public void initiallyAllPointsAreClosed() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            WinmineFieldApi api = new WinmineFieldApi(app.getWindow());
            FieldState currentField = api.getCurrentField();
            for(int x=0; x < currentField.width(); x++) {
                for(int y=0; y < currentField.height(); y++) {
                    assertThat(currentField.cellAt(new Point(x, y)), equalTo(Cell.CLOSED));
                }
            }
        } finally {
            app.close();
        }
    }

    @Test @DesignedFor(WINDOWS)
    public void leftClickingUncoversCellOrStepsOnABomb() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            WinmineFieldApi api = new WinmineFieldApi(app.getWindow());
            api.open(new Point(0, 0));
            assertThat(api.getCurrentField().cellAt(new Point(0, 0)), not(equalTo(Cell.CLOSED)));
        } catch(SteppedOnABomb ignored) {
        } finally {
            app.close();
        }
    }

    @Test @DesignedFor(WINDOWS)
    public void rightClickingMarksAsBomb() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            WinmineFieldApi api = new WinmineFieldApi(app.getWindow());
            api.markBomb(new Point(0, 0));
            assertThat(api.getCurrentField().cellAt(new Point(0, 0)), equalTo(Cell.BOMB));
        } finally {
            app.close();
        }
    }

    @Test(expected = SteppedOnABomb.class) @DesignedFor(WINDOWS)
    public void clickingAllCellsStepsOnABomb() throws Exception {
        WinmineApplication app = new WinmineApplication(User32.USER32, new ResourceLauncher());
        try {
            WinmineFieldApi api = new WinmineFieldApi(app.getWindow());

            for(int x=0; x<api.getCurrentField().width(); x++) {
                for(int y=0; y<api.getCurrentField().height(); y++) {
                    api.open(new Point(x, y));
                }
            }

        } finally {
            app.close();
        }
    }

}
