package ru.alepar.minesweeper;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;

import static org.junit.Assert.assertThat;

public class SolverTest {

    @Test
    public void solvesCaseWhereTwoIterationsAreNeeded() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("12x2x2")
                .row("x2122x")
                .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("......")
                .row(".2122.")
                .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);

        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(full));
    }

}
