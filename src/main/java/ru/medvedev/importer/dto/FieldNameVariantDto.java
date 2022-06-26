package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.XlsxRequireField;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldNameVariantDto {

    private XlsxRequireField field;
    private List<String> names = new ArrayList<>();
}
