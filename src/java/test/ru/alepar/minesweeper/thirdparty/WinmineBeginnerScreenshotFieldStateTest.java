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

public class WinmineBeginnerScreenshotFieldStateTest {

    private BufferedImage image;

    @Before
    public void setUp() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ru/alepar/minesweeper/thirdparty/winmine_beginner_screenshot.png");
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
        assertThat(recognition.bottomRight(), equalTo(new Coords(158, 247)));
    }

    @Test
    public void correctlyFindsDimensionOfTheGame() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.width(), equalTo(9));
        assertThat(recognition.height(), equalTo(9));
    }

    @Test
    public void correctlyRecognizesUncoveredZeroCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(0, 2)), equalTo(Cell.valueOf(0)));
        assertThat(recognition.cellAt(new Point(1, 2)), equalTo(Cell.valueOf(0)));
        assertThat(recognition.cellAt(new Point(1, 6)), equalTo(Cell.valueOf(0)));
    }

    @Test
    public void correctlyRecognizesUncoveredOneCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(0, 1)), equalTo(Cell.valueOf(1)));
        assertThat(recognition.cellAt(new Point(1, 1)), equalTo(Cell.valueOf(1)));
        assertThat(recognition.cellAt(new Point(2, 1)), equalTo(Cell.valueOf(1)));
    }

    @Test
    public void correctlyRecognizesUncoveredTwoCell() throws Exception {
        WinmineScreenshotFieldState recognition = new WinmineScreenshotFieldState(image);
        assertThat(recognition.cellAt(new Point(2, 5)), equalTo(Cell.valueOf(2)));
        assertThat(recognition.cellAt(new Point(4, 4)), equalTo(Cell.valueOf(2)));
        assertThat(recognition.cellAt(new Point(4, 7)), equalTo(Cell.valueOf(2)));
    }
}
