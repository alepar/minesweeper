package ru.alepar.minesweeper.ui;

import org.junit.Test;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GameControllerTest {

    @Test
    public void newGameAndLeftClickPropagatesThroughUi() {
        RecordingUi ui = new RecordingUi();
        GameController controller = new GameController(ui, Difficulty.BEGINNER, 42L);

        assertNotNull("onNewGame should fire on construction", ui.lastInitialField.get());
        assertThat(ui.lastBombsLeft.get(), equalTo(Difficulty.BEGINNER.bombs));
        assertThat(ui.lastGameState.get(), equalTo(GameState.IN_PROGRESS));

        // Click a corner; cascade opens at least that cell.
        controller.leftClick(new Point(0, 0));
        FieldState after = ui.lastField.get();
        assertNotNull(after);
        int opened = countOpened(after);
        assertThat("expected at least one cell to open after left click", opened, greaterThan(0));
    }

    @Test
    public void rightClickTogglesFlagAndUpdatesCounter() {
        RecordingUi ui = new RecordingUi();
        GameController controller = new GameController(ui, Difficulty.BEGINNER, 42L);

        int before = ui.lastBombsLeft.get();
        controller.rightClick(new Point(0, 0));
        assertThat(ui.lastBombsLeft.get(), equalTo(before - 1));
        assertThat(ui.lastField.get().cellAt(new Point(0, 0)), equalTo(Cell.BOMB));

        controller.rightClick(new Point(0, 0));
        assertThat(ui.lastBombsLeft.get(), equalTo(before));
        assertThat(ui.lastField.get().cellAt(new Point(0, 0)), equalTo(Cell.CLOSED));
    }

    @Test
    public void stepOnceProgressesAutosolve() throws Exception {
        RecordingUi ui = new RecordingUi();
        GameController controller = new GameController(ui, Difficulty.BEGINNER, 42L);

        // stepOnce runs on the EDT via the controller; it's synchronous.
        int snapshotBefore = ui.fieldUpdates.size();
        controller.stepOnce();
        // stepOnce is direct, not timer-scheduled, so the update should have fired.
        assertThat(ui.fieldUpdates.size(), greaterThan(snapshotBefore));
    }

    @Test
    public void autosolveTerminatesEitherWonOrLost() throws Exception {
        RecordingUi ui = new RecordingUi();
        CountDownLatch terminated = new CountDownLatch(1);
        ui.onTerminalState = () -> terminated.countDown();

        GameController controller = new GameController(ui, Difficulty.BEGINNER, 42L);
        controller.setAutosolveSpeed(1);
        controller.toggleAutosolve();

        if (!terminated.await(10, TimeUnit.SECONDS)) {
            fail("autosolve did not reach a terminal state within 10 seconds");
        }

        GameState end = ui.lastGameState.get();
        assertTrue("expected terminal state", end == GameState.WON || end == GameState.LOST);
    }

    private static int countOpened(FieldState f) {
        int n = 0;
        for (int y = 0; y < f.height(); y++) {
            for (int x = 0; x < f.width(); x++) {
                if (f.cellAt(new Point(x, y)).isOpened()) n++;
            }
        }
        return n;
    }

    private static final class RecordingUi implements UserInterface {
        final AtomicReference<FieldState> lastInitialField = new AtomicReference<>();
        final AtomicReference<FieldState> lastField = new AtomicReference<>();
        final java.util.concurrent.atomic.AtomicInteger lastBombsLeft = new java.util.concurrent.atomic.AtomicInteger();
        final AtomicReference<GameState> lastGameState = new AtomicReference<>();
        final List<FieldState> fieldUpdates = new ArrayList<>();
        Runnable onTerminalState;

        @Override public void onNewGame(FieldState initialField, int totalBombs) {
            lastInitialField.set(initialField);
            lastField.set(initialField);
        }
        @Override public void onFieldUpdated(FieldState field) {
            lastField.set(field);
            fieldUpdates.add(field);
        }
        @Override public void onBombsLeftChanged(int bombsLeft) { lastBombsLeft.set(bombsLeft); }
        @Override public void onGameStateChanged(GameState state) {
            lastGameState.set(state);
            if ((state == GameState.WON || state == GameState.LOST) && onTerminalState != null) {
                onTerminalState.run();
            }
        }
        @Override public void onTimerTick(int elapsedSeconds) { }
        @Override public void onAutosolveStateChanged(boolean running) { }
    }
}
