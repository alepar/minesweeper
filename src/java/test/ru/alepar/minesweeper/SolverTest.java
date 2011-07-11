package ru.alepar.minesweeper;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.fieldstate.FieldPreopener;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.SteppedOnABomb;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;

import static org.junit.Assert.assertThat;

public class SolverTest {

    @Test
    public void solvesCaseWhereFourIterationsNeededToSolve() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("1x2")
                .row("12x")
                .row(" 11")
                .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("...")
                .row("..x")
                .row("..1")
                .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);

        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(full));
    }

    @Test
    public void solvesMinesweeperMediumPreopenedCase() throws Exception {
        ArrayFieldState fullField = new FieldStateFixtureBuilder()
                .row("2x1  1xx1   112x")
                .row("x21  2331   1x21")
                .row("11   1x11121211 ")
                .row("1211 1222x3x1   ")
                .row("x2x1123x33x21 11")
                .row("12111xx22x33222x")
                .row("    122112x2xx21")
                .row("      122212332 ")
                .row("    112xx1112x1 ")
                .row("    1x22211x2121")
                .row("  11211   111 1x")
                .row("112x31  111 1121")
                .row("x12xx2  1x211x21")
                .row("1113x21122x112x2")
                .row(" 123211x1111 13x")
                .row(" 1xx1 111     2x")
            .build();

        ArrayFieldState startField = new FieldPreopener().preopen(fullField);

        FieldApi fieldApi = new SimpleFieldApi(fullField, startField);

        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(fullField ));
    }

    @Test
    public void solvesMinesweeperExpertSizedButWithSmallerAmountOfBombsPreopenedCase() throws Exception {
        ArrayFieldState fullField = new FieldStateFixtureBuilder()
                .row("   1x1    11112x3x2 1111x22x1 ")
                .row("   11211124x22x33x312x223x211 ")
                .row("11   1x11xxx22x3222x212x321   ")
                .row("x1   11123432212x1111 12x1 111")
                .row("11      2x21x1 111   11211 1x2")
                .row("      113x3332    1111x1 1122x")
                .row("    123x212xx1    2x323212x211")
                .row("11  2xx21 1332 1112x3x2x12x311")
                .row("x1  2x31 112x1 1x22121211112x1")
                .row("11  111 12x322 12x1 1222111332")
                .row("        2x43x21 111 1xx3x11xx1")
                .row("11  111 3xx22x111111234x212342")
                .row("x1  1x1 3x411111x11x22x21 1x2x")
                .row("331122114x3    12222x211113231")
                .row("xx11x212xx2 111 2x2223112x3x1 ")
                .row("22112x12x31 1x1 2x21x2x12x311 ")
            .build();

        ArrayFieldState startField = new FieldPreopener().preopen(fullField);

        FieldApi fieldApi = new SimpleFieldApi(fullField, startField);

        Solver solver = new Solver(fieldApi);
        FieldState state = solver.solve();

        assertThat(state, Matchers.<FieldState>equalTo(fullField ));
    }

    @Test
    public void whenStruckByAStateWhereMoreThanOneSolutionIsPossibleMakesAGuessAndEitherFailsOrIsLucky() throws Exception {
        ArrayFieldState fullField = new FieldStateFixtureBuilder()
                .row("x1")
                .row("11")
            .build();

        ArrayFieldState startField = new FieldStateFixtureBuilder()
                .row(".1")
                .row(".1")
            .build();

        FieldApi fieldApi = new SimpleFieldApi(fullField, startField);

        Solver solver = new Solver(fieldApi);
        try {
            assertThat(solver.solve(), Matchers.<FieldState>equalTo(fullField));
        } catch (SteppedOnABomb ignored) {
            //blowing-up is a risk we have to take when guessing
        }
    }
}
