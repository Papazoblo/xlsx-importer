package ru.medvedev.importer.exception;

public class IllegalCellTypeException extends FileProcessingException {

    public IllegalCellTypeException(String message, Long fileId) {
        super(message, fileId);
    }
}
