package ru.alepar.minesweeper.ui;

import ru.alepar.minesweeper.analyzer.ConfidentAnalyzer;
import ru.alepar.minesweeper.analyzer.LimitShuffler;
import ru.alepar.minesweeper.analyzer.MinMaxAnalyzer;
import ru.alepar.minesweeper.analyzer.ResultExecutor;
import ru.alepar.minesweeper.analyzer.TankProbabilityAnalyzer;
import ru.alepar.minesweeper.core.PointFactory;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

/**
 * Drives the solver one analyzer call at a time so the UI can animate
 * autosolve. Mirrors the Solver.solve() loop body, but yields after each
 * MinMax / tank-deduction / guess step instead of running to completion.
 */
public class SolverStepper {

    public enum Outcome {
        MIN_MAX,        // MinMax pass produced opens/marks
        TANK_DEDUCE,    // tank produced forced moves
        GUESS,          // tank picked the lowest-probability cell to open
        WON,            // no closed cells remain
        BOMB            // SteppedOnABomb thrown during the step
    }

    private final FieldApi fieldApi;
    private final LimitShuffler shuffler;
    private final PointFactory pointFactory;
    private final ResultExecutor executor;

    public SolverStepper(FieldApi fieldApi, LimitShuffler shuffler) {
        this.fieldApi = fieldApi;
        this.shuffler = shuffler;
        FieldState start = fieldApi.getCurrentField();
        this.pointFactory = new PointFactory(start.width(), start.height());
        this.executor = new ResultExecutor(fieldApi, pointFactory);
    }

    public Outcome step() {
        if (!hasClosedCells()) return Outcome.WON;
        try {
            ConfidentAnalyzer.Result mm = new MinMaxAnalyzer(
                    pointFactory, fieldApi.getCurrentField(), shuffler, null).solve();
            if (!mm.toOpen.isEmpty() || !mm.toMark.isEmpty()) {
                executor.execute(mm);
                return Outcome.MIN_MAX;
            }

            TankProbabilityAnalyzer.Analysis a = new TankProbabilityAnalyzer(
                    pointFactory, fieldApi.getCurrentField(), fieldApi.bombsLeft(), null).analyze();
            if (a.hasCertainties()) {
                executor.execute(a.certaintiesAsResult(pointFactory));
                return Outcome.TANK_DEDUCE;
            }

            fieldApi.open(a.pickLowestProbability());
            return Outcome.GUESS;
        } catch (SteppedOnABomb e) {
            return Outcome.BOMB;
        }
    }

    private boolean hasClosedCells() {
        FieldState f = fieldApi.getCurrentField();
        for (int y = 0; y < f.height(); y++) {
            for (int x = 0; x < f.width(); x++) {
                if (f.cellAt(new Point(x, y)) == Cell.CLOSED) return true;
            }
        }
        return false;
    }
}
