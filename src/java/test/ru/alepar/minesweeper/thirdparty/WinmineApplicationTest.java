package ru.alepar.minesweeper.thirdparty;

import org.junit.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class WinmineApplicationTest {

    @Test(timeout = 2000L)
    public void startsAndClosesApplicationWithoutExceptions() throws Exception {
        assertThat("please close all minesweepers before executing this test", WinmineApplication.findWinmineWindow(), nullValue());

        WinmineApplication app = new WinmineApplication();
        assertThat(WinmineApplication.findWinmineWindow(), notNullValue());
        app.close();
        assertThat(WinmineApplication.findWinmineWindow(), nullValue());
    }

}
