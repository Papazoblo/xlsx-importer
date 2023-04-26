package ru.medvedev.importer.service.sheethandler;

import lombok.Getter;
import org.apache.poi.xssf.model.SharedStringsTable;
import ru.medvedev.importer.entity.InnExcludeEntity;
import ru.medvedev.importer.entity.id.InnExcludeId;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.repository.InnExcludeRepository;

import java.util.*;
import java.util.stream.Collectors;

import static feign.Util.isNotBlank;

public class InnExcludeSheetHandler extends BaseItemSheetHandler {
    @Getter
    private final Set<String> innList = new HashSet<>();


    public InnExcludeSheetHandler(SharedStringsTable sst) {
        super(sst);
    }

    @Override
    protected int getStartIndexRowData() {
        return 2;
    }

    @Override
    protected void createItem() {

        String inn = rowMap.get("B");
        if (isNotBlank(inn)) {
            innList.add(inn);
        }
    }
}
