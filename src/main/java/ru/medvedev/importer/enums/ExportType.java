package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;

@RequiredArgsConstructor
@Getter
public enum ExportType {
    CSV("text/csv", ".csv"),
    XLSX("application/vnd.ms-excel", ".xlsx");

    private final String mediaType;
    private final String extension;

    public static ExportType fromString(String string) {
        switch (string.toUpperCase()) {
            case "CSV":
                return CSV;
            case "XLS":
            case "XLSX":
                return XLSX;
        }
        throw new EntityNotFoundException("Not fount enum with code = " + string);
    }
}
