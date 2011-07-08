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

public class WinmineExpertScreenshotRecognitionTest {

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
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.topLeft(), equalTo(new WinmineScreenshotRecognition.Coords(15, 104)));
    }

    @Test
    public void correctlyFindsBottomRightEdgeOfCellGrid() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.bottomRight(), equalTo(new WinmineScreenshotRecognition.Coords(494, 359)));
    }

    @Test
    public void correctlyFindsDimensionOfTheGame() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.width(), equalTo(30));
        assertThat(recognition.height(), equalTo(16));
    }

    @Test
    public void correctlyRecognizesUncoveredThreeCell() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.cellAt(new Point(12, 4)), equalTo(Cell.valueOf(3)));
        assertThat(recognition.cellAt(new Point(16, 9)), equalTo(Cell.valueOf(3)));
    }

    @Test
    public void correctlyRecognizesUncoveredFourCell() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.cellAt(new Point(10, 4)), equalTo(Cell.valueOf(4)));
        assertThat(recognition.cellAt(new Point(12, 6)), equalTo(Cell.valueOf(4)));
    }

    @Test
    public void correctlyRecognizesUncoveredFiveCell() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.cellAt(new Point(12, 5)), equalTo(Cell.valueOf(5)));
    }

}
