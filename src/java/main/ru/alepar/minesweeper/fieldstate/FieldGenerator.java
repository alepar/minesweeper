package ru.alepar.minesweeper.fieldstate;

import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.Point;

import java.util.Random;
import java.util.Set;

public class FieldGenerator {

    private final int width;
    private final int height;
    private final int numOfBombs;

    public FieldGenerator(int width, int height, int numOfBombs) {
        this.width = width;
        this.height = height;
        this.numOfBombs = numOfBombs;
    }

    public ArrayFieldState generate() {
        Cell[][] cells = createCellsArray();
        fillBombs(cells);
        fillNumbers(cells);
        return new ArrayFieldState(cells);
    }

    private void fillNumbers(Cell[][] cells) {
        PointFactory pointFactory = new PointFactory(width, height);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                if (cells[y][x] == null) {
                    Set<Point> neighbours = pointFactory.adjacentTo(new Point(x, y));
                    int bombCount = 0;
                    for (Point neighbour : neighbours) {
                        if (cells[neighbour.y][neighbour.x] == Cell.BOMB) {
                            bombCount++;
                        }
                    }

                    cells[y][x] = Cell.valueOf(bombCount);
                }
            }
        }
    }

    private void fillBombs(Cell[][] cells) {
        Random rnd = new Random();
        for(int bomb=0; bomb<numOfBombs; bomb++) {
            int rx;
            int ry;
            do {
                int r = rnd.nextInt(width * height);
                rx = r % height;
                ry = r / height;
            } while (cells[rx][ry] == Cell.BOMB);
            cells[rx][ry] = Cell.BOMB;
        }
    }

    private Cell[][] createCellsArray() {
        Cell[][] cells = new Cell[height][];
        for (int y=0; y<height; y++) {
            cells[y] = new Cell[width];
        }
        return cells;
    }
}
