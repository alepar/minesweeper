package ru.alepar.minesweeper.fieldstate;

import org.junit.Before;
import org.junit.Test;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.Point;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ArrayFieldStateTest {

    public static final Point MUTATION_POINT = new Point(0, 0);
    public static final Cell MUTATION_CELL = Cell.BOMB;

    private ArrayFieldState someState;
    private ArrayFieldState mutatedState;

    @Before
    public void setUp() throws Exception {
        someState = new FieldStateFixtureBuilder()
                .row("123xx.")
            .build();
        mutatedState = someState.mutate(MUTATION_POINT, MUTATION_CELL);
    }

    @Test
    public void mutateReturnsNewInstance() throws Exception {
        assertThat(mutatedState, not(sameInstance(someState)));
    }

    @Test
    public void mutateReturnsStateWithGivenCellAtGivenPoint() throws Exception {
        assertThat(mutatedState.cellAt(MUTATION_POINT), equalTo(MUTATION_CELL));
    }

    @Test
    public void mutateReturnsStateWhereAllCellsAreTheSameAsOriginalExceptCellAtMutationPoint() throws Exception {
        PointFactory factory = new PointFactory(someState.width(), someState.height());
        for (Point p : factory.allPoints()) {
            if(!p.equals(MUTATION_POINT)) {
                assertThat(mutatedState.cellAt(p), equalTo(someState.cellAt(p)));
            } else {
                assertThat(mutatedState.cellAt(p), not(equalTo(someState.cellAt(p))));
            }
        }
    }
}
