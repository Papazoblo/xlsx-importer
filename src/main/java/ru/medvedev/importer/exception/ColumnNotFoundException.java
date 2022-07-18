package ru.medvedev.importer.exception;

public class ColumnNotFoundException extends FileProcessingException {

    public ColumnNotFoundException(String message, Long fileId) {
        super(message, fileId);
    }
}
