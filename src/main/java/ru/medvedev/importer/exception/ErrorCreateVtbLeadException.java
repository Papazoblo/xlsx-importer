package ru.medvedev.importer.exception;

public class ErrorCreateVtbLeadException extends FileProcessingException {

    public ErrorCreateVtbLeadException(String message, Long fileId) {
        super(message, fileId);
    }
}
