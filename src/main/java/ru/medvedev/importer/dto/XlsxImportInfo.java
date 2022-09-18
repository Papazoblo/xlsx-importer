package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.SkorozvonField;

import java.util.*;

@Data
public class XlsxImportInfo {

    private Map<Bank, Long> banksProject = new HashMap<>();
    private Long fileId;
    private boolean enableWhatsAppLink;
    //енам скорозвона => список идентификаторов колонок экселя
    private Map<SkorozvonField, List<Integer>> fieldLinks = new LinkedHashMap<>();
    private List<String> orgTags = new ArrayList<>();
}
