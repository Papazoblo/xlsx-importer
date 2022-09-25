package ru.medvedev.importer.service.export.service;

import org.springframework.core.io.InputStreamResource;
import ru.medvedev.importer.enums.ExportType;

import java.util.Collection;

public interface SimpleExporter<T> {

    InputStreamResource exporting(ExportType type, Collection<T> data);

    InputStreamResource exporting(ExportType type, Collection<T> data, String tableName);
}
