package ru.medvedev.importer.dto.response;

import lombok.Data;

@Data
public class OpeningErrorResponse {
    private String error;
    private String description;
}
