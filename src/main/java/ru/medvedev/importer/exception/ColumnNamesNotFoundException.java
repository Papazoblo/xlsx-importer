package ru.medvedev.importer.exception;

public class ColumnNamesNotFoundException extends FileProcessingException {

    public ColumnNamesNotFoundException(String message, Long fileId) {
        super(message, fileId);
    }
}
