package ru.medvedev.importer.dto.response;

import lombok.Data;

@Data
public class VtbOpeningErrorResponse {
    private String error;
    private String description;
}
