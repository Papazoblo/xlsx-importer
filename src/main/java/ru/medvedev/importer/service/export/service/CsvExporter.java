package ru.medvedev.importer.service.export.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ColumnInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvExporter<T> extends BaseExporterImpl<T> {

    @Override
    public InputStreamResource export() {
        final String SPLITERATOR = ";";
        final Charset ENCODING = StandardCharsets.UTF_8;

        byte[] bytes = new byte[0];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, ENCODING)) {
                writer.write('\ufeff');
                writer.write(getFullTitle() + "\n");
                writer.write(getColumnList().stream().map(ColumnInfo::getName)
                        .collect(Collectors.joining(SPLITERATOR)));
                for (T record : data) {
                    String sb = "\n" + getColumnList().stream()
                            .map(column -> column.getInfoGetter().apply(record))
                            .collect(Collectors.joining(SPLITERATOR));
                    writer.write(sb);
                }
            }
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error write csv report", e);
        }
        return new InputStreamResource(new ByteArrayInputStream(bytes));
    }
}
