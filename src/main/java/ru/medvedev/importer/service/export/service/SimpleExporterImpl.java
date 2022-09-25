package ru.medvedev.importer.service.export.service;

import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import ru.medvedev.importer.enums.ExportType;
import ru.medvedev.importer.exception.BadRequestException;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
public abstract class SimpleExporterImpl<T> implements SimpleExporter<T> {

    protected TableBuilder<T> tableBuilder;

    @Override
    public InputStreamResource exporting(ExportType type, Collection<T> data, String tableName) {
        tableBuilder.generateTable();
        if (tableName != null) {
            tableBuilder.addTitle(tableName);
        }
        BaseExporter<T> exporter = null;
        switch (type) {
            case CSV:
                exporter = new CsvExporter<>();
                break;
            case XLSX:
                exporter = new XlsExporter<>();
                break;
        }
        try {
            return exporter.export(tableBuilder, data);
        } catch (IOException ex) {
            throw new BadRequestException(String.format("Ошибка экспорта данных в %s файл", type.name()), ex);
        }
    }

    @Override
    public InputStreamResource exporting(ExportType type, Collection<T> data) {
        return exporting(type, data, null);
    }

}
