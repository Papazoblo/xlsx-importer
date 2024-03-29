package ru.medvedev.importer.service.export.service;

import org.springframework.core.io.InputStreamResource;
import ru.medvedev.importer.dto.ColumnInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseExporterImpl<T> implements BaseExporter<T> {

    protected TableBuilder<T> tableBuilder;
    protected Collection<T> data;

    @Override
    public InputStreamResource export(TableBuilder<T> tableBuilder,
                                      Collection<T> data) throws IOException {
        this.tableBuilder = tableBuilder;
        this.data = data;
        return export();
    }

    protected List<ColumnInfo<T>> getColumnList() {
        return tableBuilder.getColumnList();
    }

    protected String getFullTitle() {
        return Optional.ofNullable(tableBuilder.getTitle()).map(title -> String.format("%s", title)).orElse(null);
    }

    protected String getTitle() {
        return tableBuilder.getTitle();
    }

    protected abstract InputStreamResource export() throws IOException;
}
