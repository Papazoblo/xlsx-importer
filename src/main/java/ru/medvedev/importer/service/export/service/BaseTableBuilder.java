package ru.medvedev.importer.service.export.service;


import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.dto.ColumnInfo;
import ru.medvedev.importer.dto.Table;

import java.util.List;

@Slf4j
public abstract class BaseTableBuilder<T> implements TableBuilder<T> {

    private final Table<T> table;

    public BaseTableBuilder() {
        table = new Table<>();
    }

    protected void addNewColumn(String key, ColumnInfo<T> columnInfo) {
        table.addNewColumn(key, columnInfo);
    }

    public void addTitle(String title) {
        table.addTitle(title);
    }

    public String getTitle() {
        return table.getTitle();
    }

    public void initColumnPositions(List<String> columnStates) {
        table.initColumnPositions(columnStates);
    }

    public List<ColumnInfo<T>> getColumnList() {
        return table.getColumnList();
    }

}
