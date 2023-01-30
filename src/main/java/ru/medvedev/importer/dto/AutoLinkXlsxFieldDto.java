package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.SkorozvonField;

import java.util.HashSet;
import java.util.Set;

@Data
public class AutoLinkXlsxFieldDto {

    private SkorozvonField field;
    private Set<Integer> columns = new HashSet<>();
}
