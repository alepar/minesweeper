package ru.alepar.minesweeper;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SolverTest {

    @Test
    public void runsSimpleCaseWithoutExceptions() throws Exception {
        FieldState full = new FieldStateBuilder()
                .row("1x")
            .build();

        FieldState start = new FieldStateBuilder()
                .row("1.")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        Solver solver = new Solver(fieldApi);
        solver.solve();
    }

    @Test
    public void solvesVerySimpleOneLinerCase() throws Exception {
        FieldState full = new FieldStateBuilder()
                .row("1x")
            .build();

        FieldState start = new FieldStateBuilder()
                .row("1.")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, equalTo(start));
    }
}
