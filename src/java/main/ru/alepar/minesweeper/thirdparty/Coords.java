package ru.alepar.minesweeper.thirdparty;

public final class Coords {
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

        return x == coords.x && y == coords.y;

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
