package ru.alepar.minesweeper.thirdparty;

import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import java.awt.image.BufferedImage;

public class WinmineScreenshotFieldState implements FieldState {

    private final BufferedImage image;
    private final Coords topLeft;
    private final Coords bottomRight;

    public WinmineScreenshotFieldState(BufferedImage image) {
        this.image = image;

        this.topLeft = topLeft();
        this.bottomRight = bottomRight();
    }

    Coords topLeft() {
        for(int x=0; x<image.getWidth(); x++) {
            for(int y=0; y<image.getHeight(); y++) {
                if(isStartingPointForNumberOfMinesLeft(x, y)) {
                    return new Coords(x-5, y+39);
                }
            }
        }
        throw new RuntimeException("cannot find topLeft grid corner");
    }

    Coords bottomRight() {
        int rx=-1, ry=-1;
        for(int y= topLeft.y; y<image.getHeight(); y++) {
            if(!isDarkGrey(image.getRGB(topLeft.x-1, y))) {
                ry = y-1;
                break;
            }
        }
        for(int x= topLeft.x; x<image.getWidth(); x++) {
            if(!isDarkGrey(image.getRGB(x, topLeft.y-1))) {
                rx = x-1;
                break;
            }
        }
        if(rx==-1 || ry==-1) {
            throw new RuntimeException("cannot find bottomRight grid corner");
        }
        return new Coords(rx, ry);
    }

    public int width() {
        return (bottomRight.x - topLeft.x + 1) / 16;
    }

    public int height() {
        return (bottomRight.y - topLeft.y + 1) / 16;
    }

    public Cell cellAt(Point point) {
        Coords cellTopLeft = new Coords(topLeft.x + point.x*16, topLeft.y + point.y*16);

        if(isWhite(image.getRGB(cellTopLeft.x, cellTopLeft.y))) {
            if(isRed(image.getRGB(cellTopLeft.x+6, cellTopLeft.y+6)) && isBlack(image.getRGB(cellTopLeft.x+8, cellTopLeft.y+11))) {
                return Cell.BOMB;
            }
            return Cell.CLOSED;
        }
        if(isBlue(image.getRGB(cellTopLeft.x + 8, cellTopLeft.y + 8))) {
            return Cell.valueOf(1);
        }
        if(isGreen(image.getRGB(cellTopLeft.x + 8, cellTopLeft.y + 8))) {
            return Cell.valueOf(2);
        }
        if(isRed(image.getRGB(cellTopLeft.x + 8, cellTopLeft.y + 8))) {
            return Cell.valueOf(3);
        }
        if(isDarkBlue(image.getRGB(cellTopLeft.x + 8, cellTopLeft.y + 8))) {
            return Cell.valueOf(4);
        }
        if(isDarkRed(image.getRGB(cellTopLeft.x + 8, cellTopLeft.y + 8))) {
            return Cell.valueOf(5);
        }

        return Cell.valueOf(0);
    }

    private boolean isStartingPointForNumberOfMinesLeft(int x, int y) {
        if(isBlack(image.getRGB(x, y))) {
            for(int i=0; i<39; i++) {
                if(!isBlack(image.getRGB(x+i, y))) {
                    return false;
                }
            }
            for(int i=0; i<39; i++) {
                if(isRed(image.getRGB(x + i, y + 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Coords clickCoordsForPoint(Point p) {
        return new Coords(p.x*16 + topLeft.x + 7, p.y*16 + topLeft.y + 7);
    }

    private static boolean isDarkRed(int rgb) {
        return isHexColor(rgb, 0x00800000);
    }

    private static boolean isDarkBlue(int rgb) {
        return isHexColor(rgb, 0x00000080);
    }

    private static boolean isGreen(int rgb) {
        return isHexColor(rgb, 0x00008000);
    }

    private static boolean isBlue(int rgb) {
        return isHexColor(rgb, 0x000000ff);
    }

    private static boolean isWhite(int rgb) {
        return isHexColor(rgb, 0x00ffffff);
    }

    private static boolean isDarkGrey(int rgb) {
        return isHexColor(rgb, 0x00808080);
    }

    private static boolean isRed(int rgb) {
        return isHexColor(rgb, 0x00ff0000);
    }

    private static boolean isBlack(int rgb) {
        return isHexColor(rgb, 0);
    }

    private static boolean isHexColor(int rgb, int color) {
        return (rgb & 0x00ffffff) == color;
    }

}
