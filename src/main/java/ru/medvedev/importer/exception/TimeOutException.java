package ru.medvedev.importer.exception;

public class TimeOutException extends RuntimeException {

    public TimeOutException(String message) {
        super(message);
    }

    public TimeOutException(String message, Throwable ex) {
        super(message, ex);
    }
}
