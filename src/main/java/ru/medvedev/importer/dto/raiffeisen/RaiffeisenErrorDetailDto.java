package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RaiffeisenErrorDetailDto {
    private String cause;
    private String message;
}
