package ru.alepar.minesweeper.thirdparty;

import java.awt.image.BufferedImage;

public class WinmineScreenshotRecognition {

    private final BufferedImage image;

    public WinmineScreenshotRecognition(BufferedImage image) {
        this.image = image;
    }

    public Coords topLeft() {
        for(int x=0; x<image.getWidth(); x++) {
            for(int y=0; y<image.getHeight(); y++) {
                if(isStartingPointForNumberOfMinesLeft(x, y)) {
                    return new Coords(x-5, y+39);
                }
            }
        }
        throw new RuntimeException("cannot find topLeft grid corner");
    }

    public Coords bottomRight() {
        Coords topLeft = topLeft();
        int rx=-1, ry=-1;
        for(int y=topLeft.y; y<image.getHeight(); y++) {
            if(!isDarkGrey(image.getRGB(topLeft.x-1, y))) {
                ry = y-1;
                break;
            }
        }
        for(int x=topLeft.x; x<image.getWidth(); x++) {
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

    public Integer width() {
        return (bottomRight().x - topLeft().x + 1) / 16;
    }

    public Integer height() {
        return (bottomRight().y - topLeft().y + 1) / 16;
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

    private boolean isDarkGrey(int rgb) {
        return (rgb & 0x00ffffff) == 0x00808080;
    }

    private boolean isRed(int rgb) {
        return (rgb & 0x00ffffff) == 0x00ff0000;
    }

    private static boolean isBlack(int rgb) {
        return (rgb & 0x00ffffff) == 0;
    }

    public static final class Coords {
        public final int x, y;

        public Coords(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coords coords = (Coords) o;

            if (x != coords.x) return false;
            if (y != coords.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public String toString() {
            return "Coords{" +
                    x +
                    ", " + y +
                    '}';
        }
    }
}
