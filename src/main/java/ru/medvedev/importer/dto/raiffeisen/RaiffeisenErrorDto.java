package ru.medvedev.importer.dto.raiffeisen;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RaiffeisenErrorDto {

    private String errorCode;
    private String errorMessage;
    private List<RaiffeisenErrorDetailDto> details = new ArrayList<>();
}
