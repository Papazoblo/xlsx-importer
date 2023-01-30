package ru.medvedev.importer.exception;

public class InnListIsEmptyException extends FileProcessingException {

    public InnListIsEmptyException(String message, Long fileId) {
        super(message, fileId);
    }
}
