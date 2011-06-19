package ru.alepar.minesweeper;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FieldGeneratorTest {

    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;
    private static final int BOMBS = 40;

    private final FieldGenerator generator = new FieldGenerator(WIDTH, HEIGHT, BOMBS);
    private final FieldState field = generator.generate();

    @Test
    public void makesFieldWithProperSizes() throws Exception {
        assertThat(field.height(), equalTo(HEIGHT));
        assertThat(field.width(), equalTo(WIDTH));
    }

    @Test
    public void makesFieldWithProperNumberOfBombsOnIt() throws Exception {
        int bombCount=0;
        for (int y=0; y<field.height(); y++) {
            for (int x=0; x<field.width(); x++) {
                if(field.cellAt(new Point(x, y)) == Cell.BOMB) {
                    bombCount++;
                }
            }
        }
        assertThat(bombCount, equalTo(BOMBS));
    }

    @Test
    public void makesFieldFullyPopulated() throws Exception {
        for (int y=0; y<field.height(); y++) {
            for (int x=0; x<field.width(); x++) {
                Cell cell = field.cellAt(new Point(x, y));
                assertThat(cell, allOf(not(equalTo(Cell.CLOSED)), notNullValue()));
            }
        }
    }

    @Test
    public void makesFieldWithNumberCellsProperlyPlaced() throws Exception {
        PointFactory pointFactory = new PointFactory(field.width(), field.height());

        for (int y=0; y<field.height(); y++) {
            for (int x=0; x<field.width(); x++) {
                Point curPoint = new Point(x, y);
                Cell curCell = field.cellAt(curPoint);
                if(curCell.isOpened()) {
                    int numOfNeighbourBombs = 0;
                    for (Point neighbour : pointFactory.adjacentTo(curPoint)) {
                        if(field.cellAt(neighbour) == Cell.BOMB) {
                            numOfNeighbourBombs++;
                        }
                    }
                    if(curCell.value != numOfNeighbourBombs) {
                        throw new RuntimeException("number of bombs doesnt match for " + curPoint);
                    }
                }
            }
        }
    }
}
