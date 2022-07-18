package ru.medvedev.importer.exception;

public class NumberProjectNotFoundException extends FileProcessingException {

    public NumberProjectNotFoundException(String message, Long fileId) {
        super(message, fileId);
    }
}
