package ru.alepar.minesweeper.analyzer;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
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

        Set<Point> toMark = new HashSet<Point>() {{
            add(new Point(1, 0));
        }};
        FieldAnalyzer.Result expected = new FieldAnalyzer.Result(toMark, Collections.<Point>emptySet());

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height()), fieldApi.getCurrentField());
        FieldAnalyzer.Result result = analyzer.solve();

        assertThat(result, equalTo(expected));
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

        Set<Point> toMark = new HashSet<Point>() {{
            add(new Point(1, 0));
            add(new Point(2, 0));
            add(new Point(3, 0));
        }};
        FieldAnalyzer.Result expected = new FieldAnalyzer.Result(toMark, Collections.<Point>emptySet());

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height()), fieldApi.getCurrentField());
        FieldAnalyzer.Result result = analyzer.solve();

        assertThat(result, equalTo(expected));
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

        Set<Point> toMark = new HashSet<Point>() {{
            add(new Point(4, 0));
        }};
        Set<Point> toOpen = new HashSet<Point>() {{
            add(new Point(1, 0));
        }};
        FieldAnalyzer.Result expected = new FieldAnalyzer.Result(toMark, toOpen);

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height()), fieldApi.getCurrentField());
        FieldAnalyzer.Result result = analyzer.solve();

        assertThat(result, equalTo(expected));
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

        Set<Point> toMark = new HashSet<Point>() {{
            add(new Point(3, 0));
        }};
        Set<Point> toOpen = new HashSet<Point>() {{
            add(new Point(0, 0));
            add(new Point(1, 0));
            add(new Point(2, 0));
            add(new Point(4, 0));
        }};
        FieldAnalyzer.Result expected = new FieldAnalyzer.Result(toMark, toOpen);

        FieldApi fieldApi = new SimpleFieldApi(full, start);
        FieldAnalyzer analyzer = new MinMaxConfidentAnalyzer(new PointFactory(fieldApi.getCurrentField().width(), fieldApi.getCurrentField().height()), fieldApi.getCurrentField());
        FieldAnalyzer.Result result = analyzer.solve();

        assertThat(result, equalTo(expected));
    }
}
