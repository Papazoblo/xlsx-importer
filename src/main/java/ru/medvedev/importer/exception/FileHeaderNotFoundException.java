package ru.medvedev.importer.exception;

public class FileHeaderNotFoundException extends FileProcessingException {

    public FileHeaderNotFoundException(String message, Long fileId) {
        super(message, fileId);
    }
}
