package ru.alepar.minesweeper;

import ch.qos.logback.classic.Level;
import ru.alepar.minesweeper.analyzer.SubtractIntersectLimitShuffler;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.fieldstate.FieldGenerator;
import ru.alepar.minesweeper.fieldstate.FieldPreopener;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldState;
import ru.alepar.minesweeper.model.Point;
import ru.alepar.minesweeper.model.SteppedOnABomb;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;

public class Benchmark {

    // Expert: 30 wide, 16 tall, 99 bombs.
    private static final int WIDTH = 30;
    private static final int HEIGHT = 16;
    private static final int BOMBS = 99;

    private static final int GAMES = 1000;
    private static final long MASTER_SEED = 42L;

    public static void main(String[] args) {
        silenceAnalyzerLogs();

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isCurrentThreadCpuTimeSupported()) {
            throw new RuntimeException("current-thread CPU time measurement is not supported on this JVM");
        }

        Random master = new Random(MASTER_SEED);
        int totalNonBombCells = WIDTH * HEIGHT - BOMBS;

        long totalOpenedNonBomb = 0;
        long totalCpuNanos = 0;
        int wins = 0;
        int bombStrikes = 0;

        for (int g = 0; g < GAMES; g++) {
            // Per-game Random so adding/removing games doesn't reshuffle the others.
            Random gameRandom = new Random(master.nextLong());
            ArrayFieldState full = new FieldGenerator(WIDTH, HEIGHT, BOMBS, gameRandom).generate();
            ArrayFieldState start = new FieldPreopener().preopen(full);
            SimpleFieldApi api = new SimpleFieldApi(full, start);
            Solver solver = new Solver(api, new SubtractIntersectLimitShuffler(), null);

            int openedAtEnd;
            long t0 = bean.getCurrentThreadCpuTime();
            try {
                FieldState end = solver.solve();
                openedAtEnd = countOpened(end);
                if (openedAtEnd == totalNonBombCells) {
                    wins++;
                }
            } catch (SteppedOnABomb e) {
                bombStrikes++;
                openedAtEnd = countOpened(api.getCurrentField());
            }
            totalCpuNanos += bean.getCurrentThreadCpuTime() - t0;
            totalOpenedNonBomb += openedAtEnd;
        }

        double avgScore = (double) totalOpenedNonBomb / GAMES / totalNonBombCells;
        double avgCpuMs = totalCpuNanos / 1_000_000.0 / GAMES;

        System.out.println();
        System.out.printf("Board:         %dx%d, %d bombs (%d non-bomb cells)%n",
                WIDTH, HEIGHT, BOMBS, totalNonBombCells);
        System.out.printf("Games:         %d (master seed %d)%n", GAMES, MASTER_SEED);
        System.out.printf("Wins:          %d (%.2f%%)%n", wins, 100.0 * wins / GAMES);
        System.out.printf("Bomb strikes:  %d (%.2f%%)%n", bombStrikes, 100.0 * bombStrikes / GAMES);
        System.out.printf("Avg score:     %.4f  (non-bomb cells opened / non-bomb cells)%n", avgScore);
        System.out.printf("Total CPU:     %.2f s%n", totalCpuNanos / 1e9);
        System.out.printf("Avg CPU/game:  %.2f ms%n", avgCpuMs);
    }

    private static int countOpened(FieldState field) {
        int count = 0;
        for (int y = 0; y < field.height(); y++) {
            for (int x = 0; x < field.width(); x++) {
                if (field.cellAt(new Point(x, y)).isOpened()) {
                    count++;
                }
            }
        }
        return count;
    }

    // The analyzers log heavily at WARN; benchmarking 1000 games with that
    // enabled costs both noise and meaningful CPU.
    private static void silenceAnalyzerLogs() {
        ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("ru.alepar");
        root.setLevel(Level.ERROR);
    }

}
