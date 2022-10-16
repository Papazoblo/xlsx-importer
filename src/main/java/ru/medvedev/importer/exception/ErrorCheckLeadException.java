package ru.medvedev.importer.exception;

public class ErrorCheckLeadException extends FileProcessingException {

    public ErrorCheckLeadException(String message, Long fileId) {
        super(message, fileId);
    }
}
