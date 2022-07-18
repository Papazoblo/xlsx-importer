package ru.medvedev.importer.dto;

import lombok.Data;

@Data
public class FieldPositionDto {
    private Integer position;
    private boolean required = false;
}
