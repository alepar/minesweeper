package ru.alepar.minesweeper.thirdparty;

import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ru.alepar.minesweeper.Solver;
import ru.alepar.minesweeper.analyzer.CountingLimitShuffler;
import ru.alepar.minesweeper.analyzer.LimitShuffler;
import ru.alepar.minesweeper.analyzer.SubtractIntersectLimitShuffler;
import ru.alepar.minesweeper.core.SimpleFieldApi;
import ru.alepar.minesweeper.fieldstate.ArrayFieldState;
import ru.alepar.minesweeper.fieldstate.FieldPreopener;
import ru.alepar.minesweeper.model.Cell;
import ru.alepar.minesweeper.model.FieldApi;
import ru.alepar.minesweeper.model.FieldState;

public class WinmineApplication {

    private static final String WINMINE_WINDOW_CAPTION = "Minesweeper";
    private static final String WINMINE_RESOURCE = "winmine.exe";

    private final User32 user32;
    private final Window wnd;

    public WinmineApplication(User32 user32, ResourceLauncher launcher) {
        this.user32 = user32;

        assertThereAreNoMinesweepersRunning();
        launcher.launch(WINMINE_RESOURCE);

        while(findWinmineWindow() == null) { safeSleep(50l); }
        safeSleep(500l); //allow winmine to settle and draw itself
        this.wnd = new MicrosoftWindow(this.user32, findWinmineWindow());
    }

    public Window getWindow() {
        return wnd;
    }

    private void assertThereAreNoMinesweepersRunning() {
        if (findWinmineWindow() != null) {
            throw new RuntimeException("another minesweeper is already running");
        }
    }

    public HWND findWinmineWindow() {
        return user32.FindWindow(null, WINMINE_WINDOW_CAPTION);
    }

    public static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  //propagate interrupt further
        }
    }

    public void close() {
        wnd.close();
    }

    public static void main(String[] args) throws Exception {
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

        CountingLimitShuffler limitShuffler = new CountingLimitShuffler(new SubtractIntersectLimitShuffler());

        Date start = new Date();
        FieldApi fieldApi = new SimpleFieldApi(fullField, startField);

        Solver solver = new Solver(fieldApi, limitShuffler);
        solver.solve();
        Date end = new Date();

        System.out.println("took " + (end.getTime() - start.getTime()) + "ms");
        System.out.println("made shuffles " + limitShuffler.getCount());
    }

    public static class FieldStateFixtureBuilder {

    private static final Map<Character, Cell> translateMap;

    static {
        translateMap = new HashMap<Character, Cell>();
        translateMap.put('.', Cell.CLOSED);
        translateMap.put('x', Cell.BOMB);
        translateMap.put(' ', Cell.valueOf(0));
        translateMap.put('1', Cell.valueOf(1));
        translateMap.put('2', Cell.valueOf(2));
        translateMap.put('3', Cell.valueOf(3));
        translateMap.put('4', Cell.valueOf(4));
        translateMap.put('5', Cell.valueOf(5));
        translateMap.put('6', Cell.valueOf(6));
        translateMap.put('7', Cell.valueOf(7));
        translateMap.put('8', Cell.valueOf(8));
    }

    private final List<String> rows = new LinkedList<String>();

    public FieldStateFixtureBuilder row(String row) {
        rows.add(row);
        return this;
    }

    public ArrayFieldState build() {
        Integer rowLength = null;
        Cell[][] cells = null;
        int i = 0;
        for (String row : rows) {
            if (cells == null) {
                cells = new Cell[rows.size()][];
                rowLength = row.length();
            }

            if (rowLength != row.length()) {
                throw new IllegalArgumentException("inconsistent row lengthes");
            }
            cells[i] = new Cell[rowLength];

            for (int j = 0; j < row.length(); j++) {
                cells[i][j] = translate(row.charAt(j));
            }
            i++;
        }
        return new ArrayFieldState(cells);
    }

    private static Cell translate(char c) {
        Cell cell = translateMap.get(c);
        if (cell != null) {
            return cell;
        }
        throw new IllegalArgumentException("cannot translate char = " + c);
    }
}


}
