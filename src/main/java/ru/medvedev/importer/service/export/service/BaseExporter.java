package ru.medvedev.importer.service.export.service;

import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.util.Collection;

public interface BaseExporter<T> {

    InputStreamResource export(TableBuilder<T> table, Collection<T> data) throws IOException;

}
