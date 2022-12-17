package ru.medvedev.importer.dto;


import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Table<T> {

    private String title;

    private Map<String, ColumnInfo<T>> columnInfos;
    private List<ColumnInfo<T>> columnList;

    public Table() {
        columnInfos = new LinkedHashMap<>();
    }

    public void addTitle(String title) {
        this.title = title;
    }

    public void addNewColumn(String key, ColumnInfo<T> columnInfo) {
        columnInfos.put(key, columnInfo);
    }

    public void initColumnPositions(List<String> columnStates) {
        columnList = new ArrayList<>();
        if (columnStates.isEmpty()) {
            initColumnList(columnInfos.keySet());
        } else {
            initColumnList(columnStates);
        }
    }

    public String getTitle() {
        return this.title;
    }

    public List<ColumnInfo<T>> getColumnList() {
        return new ArrayList<>(this.columnInfos.values());
    }

    private void initColumnList(Collection<String> columnStates) {
        columnStates.forEach(name -> {
            Optional<ColumnInfo<T>> columnInfo = Optional.ofNullable(columnInfos.get(name));
            if (columnInfo.isPresent()) {
                columnList.add(columnInfo.get());
            } else {
                log.debug(String.format("Столбец с ключом [%s] не найден", name));
            }
        });
    }
}
