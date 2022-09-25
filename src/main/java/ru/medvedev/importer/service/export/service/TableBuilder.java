package ru.medvedev.importer.service.export.service;

import ru.medvedev.importer.dto.ColumnInfo;

import java.util.List;

public interface TableBuilder<T> {

    void generateTable();

    String getTitle();

    void addTitle(String title);

    void initColumnPositions(List<String> columnStates);

    List<ColumnInfo<T>> getColumnList();
}
