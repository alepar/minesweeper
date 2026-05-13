package ru.alepar.minesweeper.ui;

import ru.alepar.minesweeper.analyzer.SubtractIntersectLimitShuffler;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.fieldstate.FieldGenerator;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

import javax.swing.Timer;
import java.util.Random;

/**
 * Owns the game state, mediates input from the UI, and drives the autosolve
 * stepper on a Swing {@link Timer}. All public methods are expected to be
 * called on the EDT (the Swing UI's mouse listeners and timers run on the
 * EDT anyway). UI updates are pushed through the injected {@link UserInterface}.
 */
public class GameController {

    private final UserInterface ui;
    private Difficulty difficulty;
    private long seedForCurrentGame;

    private GameFieldApi fieldApi;
    private GameState state;

    private long gameStartMs;
    private boolean timerStarted;
    private final Timer secondTickTimer;

    private SolverStepper stepper;
    private Timer autosolveTimer;
    private int autosolveDelayMs = 200;

    public GameController(UserInterface ui, Difficulty initial, long seed) {
        this.ui = ui;
        this.secondTickTimer = new Timer(250, e -> tickTimer());
        this.secondTickTimer.start();
        startNewGame(initial, seed);
    }

    public void newGame(Difficulty d, long seed) {
        stopAutosolve();
        startNewGame(d, seed);
    }

    public void newGame(Difficulty d) {
        newGame(d, System.nanoTime());
    }

    public void restart() {
        newGame(difficulty, seedForCurrentGame);
    }

    public void leftClick(Point p) {
        if (state != GameState.IN_PROGRESS) return;
        startTimerIfNeeded();
        try {
            fieldApi.open(p);
        } catch (SteppedOnABomb e) {
            lose();
            return;
        }
        ui.onFieldUpdated(fieldApi.getCurrentField());
        ui.onBombsLeftChanged(displayBombsLeft());
        if (fieldApi.allNonBombsOpened()) win();
    }

    public void rightClick(Point p) {
        if (state != GameState.IN_PROGRESS) return;
        startTimerIfNeeded();
        if (fieldApi.getCurrentField().cellAt(p).isOpened()) return;
        if (isFlagged(p)) {
            fieldApi.unmarkBomb(p);
        } else {
            fieldApi.markBomb(p);
        }
        ui.onFieldUpdated(fieldApi.getCurrentField());
        ui.onBombsLeftChanged(displayBombsLeft());
    }

    public void toggleAutosolve() {
        if (autosolveTimer != null && autosolveTimer.isRunning()) {
            stopAutosolve();
            return;
        }
        if (state != GameState.IN_PROGRESS) return;
        startTimerIfNeeded();
        ensureStepper();
        if (autosolveTimer == null) {
            autosolveTimer = new Timer(autosolveDelayMs, e -> doStep());
        } else {
            autosolveTimer.setDelay(autosolveDelayMs);
        }
        autosolveTimer.start();
        ui.onAutosolveStateChanged(true);
    }

    public void stepOnce() {
        if (state != GameState.IN_PROGRESS) return;
        startTimerIfNeeded();
        ensureStepper();
        doStep();
    }

    public void setAutosolveSpeed(int delayMs) {
        this.autosolveDelayMs = Math.max(1, delayMs);
        if (autosolveTimer != null) autosolveTimer.setDelay(this.autosolveDelayMs);
    }

    public Difficulty difficulty() { return difficulty; }

    private void startNewGame(Difficulty d, long seed) {
        this.difficulty = d;
        this.seedForCurrentGame = seed;
        ArrayFieldState truth = new FieldGenerator(d.width, d.height, d.bombs, new Random(seed)).generate();
        this.fieldApi = new GameFieldApi(truth);
        this.state = GameState.IN_PROGRESS;
        this.timerStarted = false;
        this.gameStartMs = 0;
        this.stepper = null;
        ui.onNewGame(fieldApi.getCurrentField(), d.bombs);
        ui.onBombsLeftChanged(displayBombsLeft());
        ui.onGameStateChanged(state);
        ui.onTimerTick(0);
        ui.onAutosolveStateChanged(false);
    }

    private void ensureStepper() {
        if (stepper == null) {
            stepper = new SolverStepper(fieldApi, new SubtractIntersectLimitShuffler());
        }
    }

    private void doStep() {
        if (state != GameState.IN_PROGRESS) {
            stopAutosolve();
            return;
        }
        SolverStepper.Outcome outcome = stepper.step();
        ui.onFieldUpdated(fieldApi.getCurrentField());
        ui.onBombsLeftChanged(displayBombsLeft());
        switch (outcome) {
            case BOMB: lose(); break;
            case WON:  win();  break;
            default:
                if (fieldApi.allNonBombsOpened()) win();
                break;
        }
    }

    private void stopAutosolve() {
        if (autosolveTimer != null && autosolveTimer.isRunning()) {
            autosolveTimer.stop();
            ui.onAutosolveStateChanged(false);
        }
    }

    private void win() {
        if (state != GameState.IN_PROGRESS) return;
        state = GameState.WON;
        stopAutosolve();
        ui.onGameStateChanged(state);
    }

    private void lose() {
        if (state != GameState.IN_PROGRESS) return;
        state = GameState.LOST;
        fieldApi.revealAllBombs();
        stopAutosolve();
        ui.onFieldUpdated(fieldApi.getCurrentField());
        ui.onGameStateChanged(state);
    }

    private boolean isFlagged(Point p) {
        return fieldApi.getCurrentField().cellAt(p) == ru.alepar.minesweeper.model.Cell.BOMB;
    }

    private int displayBombsLeft() {
        return fieldApi.totalBombs() - fieldApi.flaggedCount();
    }

    private void startTimerIfNeeded() {
        if (!timerStarted) {
            timerStarted = true;
            gameStartMs = System.currentTimeMillis();
        }
    }

    private void tickTimer() {
        if (state == GameState.IN_PROGRESS && timerStarted) {
            int seconds = (int) ((System.currentTimeMillis() - gameStartMs) / 1000);
            ui.onTimerTick(seconds);
        }
    }
}
