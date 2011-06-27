package ru.alepar.minesweeper.thirdparty;

public class NativeException extends Exception {

    public NativeException() {
    }

    public NativeException(String message) {
        super(message);
    }

    public NativeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NativeException(Throwable cause) {
        super(cause);
    }
}
