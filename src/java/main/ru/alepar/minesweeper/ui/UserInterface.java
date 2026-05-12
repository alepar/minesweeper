package ru.alepar.minesweeper.ui;

import ru.alepar.minesweeper.model.FieldState;

/**
 * Output channel that GameController pushes updates to. Implementations are
 * expected to marshal onto whatever thread they need (e.g. the Swing EDT) --
 * GameController makes no thread-safety guarantees about the calling thread.
 */
public interface UserInterface {

    /** Field geometry changed (new game). Width/height come from the field. */
    void onNewGame(FieldState initialField, int totalBombs);

    /** Visible field state changed (cell opened / flagged / unflagged). */
    void onFieldUpdated(FieldState field);

    /** Bombs-remaining counter changed. Equals totalBombs - flaggedCells. */
    void onBombsLeftChanged(int bombsLeft);

    /** Game state transition (IN_PROGRESS / WON / LOST). */
    void onGameStateChanged(GameState state);

    /** Seconds since this game started. Fired roughly once per second. */
    void onTimerTick(int elapsedSeconds);

    /** Autosolve play/pause state changed. */
    void onAutosolveStateChanged(boolean running);
}
