package ru.alepar.minesweeper;

import org.junit.Test;

public class SolverTest {

    @Test
    public void runsSimpleCaseWithoutExceptions() throws Exception {
        FieldApi fieldApi = new SimpleFieldApi(new FieldState());
        Solver solver = new Solver(fieldApi);
        solver.solve();
    }

    @Test
    public void solvesVerySimpleCase() throws Exception {
        FieldApi fieldApi = new SimpleFieldApi(new FieldState());
        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();
    }
}
