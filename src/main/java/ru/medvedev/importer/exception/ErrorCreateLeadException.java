package ru.medvedev.importer.exception;

public class ErrorCreateLeadException extends FileProcessingException {

    public ErrorCreateLeadException(String message, Long fileId) {
        super(message, fileId);
    }
}
