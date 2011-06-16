package ru.alepar.minesweeper;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class SolverTest {

    @Test
    public void runsSimpleCaseWithoutExceptions() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("1x")
            .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("1.")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        Solver solver = new Solver(fieldApi);
        solver.solve();
    }

    @Test
    public void solvesVerySimpleOneLinerCase() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("1x")
            .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("1.")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(full));
    }

    @Test
    public void solvesCaseWithMixedOpenedClosedNeighbours() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("1xxx")
                .row("1232")
            .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("1...")
                .row("1232")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(full));
    }

    @Test
    public void solvesCaseWithWithIntersectingLimits() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("12x12x")
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
