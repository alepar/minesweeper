package ru.alepar.minesweeper.analyzer;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;

import static org.junit.Assert.assertThat;

public class FieldAnalyzerTest {

    @Test
    public void solvesVerySimpleOneLinerCase() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("1x")
                .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("1.")
                .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(fieldApi);
        FieldState state = analyzer.solve();

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
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(fieldApi);
        FieldState state = analyzer.solve();

        assertThat(state, Matchers.<FieldState>equalTo(full));
    }

    @Test
    public void solvesCaseWithIntersectingLimits() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("12x2x2")
                .row("x2122x")
                .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row("......")
                .row(".2122.")
                .build();

        ArrayFieldState expected = new FieldStateFixtureBuilder()
                .row(".2..x.")
                .row(".2122.")
                .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(fieldApi);
        FieldState state = analyzer.solve();

        assertThat(state, Matchers.<FieldState>equalTo(expected));
    }

    @Test
    public void solvesCaseWithOneOfBombsAlreadyUncovered() throws Exception {
        ArrayFieldState full = new FieldStateFixtureBuilder()
                .row("111x1")
                .row("x1111")
                .build();

        ArrayFieldState start = new FieldStateFixtureBuilder()
                .row(".....")
                .row("x1111")
                .build();

        ArrayFieldState expected = new FieldStateFixtureBuilder()
                .row("111x1")
                .row("x1111")
                .build();

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(fieldApi);
        FieldState state = analyzer.solve();

        assertThat(state, Matchers.<FieldState>equalTo(expected));
    }
}
