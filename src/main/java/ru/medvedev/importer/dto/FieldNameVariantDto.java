package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.XlsxRequireField;

import java.util.HashSet;
import java.util.Set;

@Data
public class FieldNameVariantDto {

    private XlsxRequireField field;
    private Set<String> names = new HashSet<>();
}
