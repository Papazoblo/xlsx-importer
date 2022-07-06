package ru.medvedev.importer.exception;

public class HeaderNotFoundException extends FileProcessingException {

    public HeaderNotFoundException(String message, Long fileId) {
        super(message, fileId);
    }
}
