package ru.alepar.minesweeper.thirdparty;

import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class WinmineBeginnerScreenshotRecognitionTest {

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
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.topLeft(), equalTo(new WinmineScreenshotRecognition.Coords(15, 104)));
    }

    @Test
    public void correctlyFindsBottomRightEdgeOfCellGrid() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.bottomRight(), equalTo(new WinmineScreenshotRecognition.Coords(158, 247)));
    }

    @Test
    public void correctlyFindsDimensionOfTheGame() throws Exception {
        WinmineScreenshotRecognition recognition = new WinmineScreenshotRecognition(image);
        assertThat(recognition.width(), equalTo(9));
        assertThat(recognition.height(), equalTo(9));
    }
}
