package ru.alepar.minesweeper.analyzer;

import org.junit.Test;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.testsupport.FieldStateFixtureBuilder;

import java.io.StringWriter;
import java.io.Writer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class LowestProbabilityAnalyzerTest {

    // Field: opened "1" at (0,0); three closed cells at (1,0), (2,0), (3,0).
    // The "1" forces P(bomb at (1,0)) = 1.0 (limit {(1,0)} = 1 bomb).
    // (2,0) and (3,0) are inner cells with no opened neighbour, so they
    // get the inner probability bombsLeft / |inner| = 1/2 = 0.5 each.
    // A "lowest probability" picker must pick one of the 0.5 cells, never (1,0).
    @Test
    public void picksSafestCellNotMostBombProbableOne() {
        ArrayFieldState state = new FieldStateFixtureBuilder()
                .row("1...")
                .build();

        PointFactory pf = new PointFactory(state.width(), state.height());
        Writer writer = new StringWriter();
        LowestProbabilityAnalyzer analyzer =
                new LowestProbabilityAnalyzer(pf, state, 1, writer);

        Point pick = analyzer.guessWhatToOpen();

        assertThat(pick, not(equalTo(new Point(1, 0))));
    }

    // C(30, 10) = 30045015. The intermediate falling factorial
    // 21*22*...*30 = ~1.09e13 overflows a 32-bit accumulator long
    // before the division loop can bring it back into range.
    @Test
    public void binomialCoefficientDoesNotOverflowForModerateRegions() {
        assertThat(LowestProbabilityAnalyzer.c(30, 10), equalTo(30045015L));
    }
}
