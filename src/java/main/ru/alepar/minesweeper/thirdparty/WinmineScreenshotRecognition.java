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
    }
}
