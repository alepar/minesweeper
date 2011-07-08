package ru.alepar.minesweeper.thirdparty;

import org.junit.Before;
import org.junit.Test;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.Point;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class WinmineExpertScreenshotFieldStateTest {

    private BufferedImage image;

    @Before
    public void setUp() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ru/alepar/minesweeper/thirdparty/winmine_expert_screenshot.png");
        if (stream == null) {
            throw new RuntimeException("could not find test screenshot");
        }
        image = ImageIO.read(stream);
    }

    @Test
    public void correctlyFindsTopLeftEdgeOfCellGrid() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.topLeft(), equalTo(new Coords(15, 104)));
    }

    @Test
    public void correctlyFindsBottomRightEdgeOfCellGrid() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.bottomRight(), equalTo(new Coords(494, 359)));
    }

    @Test
    public void correctlyFindsDimensionOfTheGame() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.width(), equalTo(30));
        assertThat(recognition.height(), equalTo(16));
    }

    @Test
    public void correctlyRecognizesBombCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(11, 4)), equalTo(Cell.BOMB));
        assertThat(recognition.cellAt(new Point(11, 5)), equalTo(Cell.BOMB));
        assertThat(recognition.cellAt(new Point(13, 5)), equalTo(Cell.BOMB));
    }

    @Test
    public void correctlyRecognizesUncoveredThreeCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(12, 4)), equalTo(Cell.valueOf(3)));
        assertThat(recognition.cellAt(new Point(16, 9)), equalTo(Cell.valueOf(3)));
    }

    @Test
    public void correctlyRecognizesUncoveredFourCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(10, 4)), equalTo(Cell.valueOf(4)));
        assertThat(recognition.cellAt(new Point(12, 6)), equalTo(Cell.valueOf(4)));
    }

    @Test
    public void correctlyRecognizesUncoveredFiveCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(12, 5)), equalTo(Cell.valueOf(5)));
    }

    @Test
    public void correctlyRecognizesUncoveredSixCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(10, 5)), equalTo(Cell.valueOf(6)));
    }

    @Test
    public void correctlyRecognizesUncoveredSevenCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(8, 7)), equalTo(Cell.valueOf(7)));
    }

    @Test(expected = RuntimeException.class)
    public void correctlyReportsUnrecognizedCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);

        recognition.cellAt(new Point(10, 0));
    }
}
