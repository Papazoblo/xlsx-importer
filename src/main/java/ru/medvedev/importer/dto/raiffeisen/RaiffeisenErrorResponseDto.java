package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class RaiffeisenErrorResponseDto {

    private Map<String, String> meta;
    private RaiffeisenErrorDto error;
}
