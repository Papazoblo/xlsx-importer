package ru.medvedev.importer.service.export.exporter;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.entity.CheckLeadInfoEntity;
import ru.medvedev.importer.service.export.service.SimpleExporterImpl;
import ru.medvedev.importer.service.export.service.TableBuilder;

@Component
public class CheckLeadResultExporterService extends SimpleExporterImpl<CheckLeadInfoEntity> {

    public CheckLeadResultExporterService(TableBuilder<CheckLeadInfoEntity> tableBuilder) {
        super(tableBuilder);
    }
}
