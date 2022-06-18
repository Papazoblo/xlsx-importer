package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.SkorozvonField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class XlsxImportInfo {

    private Long projectCode;
    //енам скорозвона => список идентификаторов колонок экселя
    private Map<SkorozvonField, List<Integer>> fieldLinks = new LinkedHashMap<>();
    private List<String> orgTags = new ArrayList<>();
}
