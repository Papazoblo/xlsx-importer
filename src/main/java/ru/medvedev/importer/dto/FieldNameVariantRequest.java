package ru.medvedev.importer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldNameVariantRequest {

    private List<FieldNameVariantDto> data = new ArrayList<>();
}
