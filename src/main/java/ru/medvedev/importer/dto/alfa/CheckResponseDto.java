package ru.medvedev.importer.dto.alfa;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CheckResponseDto {
    private List<CheckErrorDto> errors = new ArrayList<>();
}
