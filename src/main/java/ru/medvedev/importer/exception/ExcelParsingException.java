package ru.medvedev.importer.exception;

public class ExcelParsingException extends RuntimeException {

    public ExcelParsingException(String message) {
        super(message);
    }

    public ExcelParsingException(String message, Throwable ex) {
        super(message, ex);
    }
}
