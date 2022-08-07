package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.XlsxRequireField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ColumnInfoDto {

    private Map<XlsxRequireField, FieldPositionDto> fieldPositionMap = new HashMap<>();
    //первые N строк столбца в позиции key
    private Map<Integer, List<String>> columnInfoMap = new HashMap<>();
}
