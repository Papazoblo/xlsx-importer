package ru.medvedev.importer.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileProcessingException extends RuntimeException {

    private final Long fileId;

    public FileProcessingException(String message, Long fileId) {
        super(message);
        this.fileId = fileId;
    }
}
