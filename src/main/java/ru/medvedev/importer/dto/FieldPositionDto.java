package ru.medvedev.importer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldPositionDto {
    private List<HeaderDto> header = new ArrayList<>();
    private boolean required = false;
}
