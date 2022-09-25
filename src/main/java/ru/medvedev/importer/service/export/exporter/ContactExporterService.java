package ru.medvedev.importer.service.export.exporter;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.service.export.service.SimpleExporterImpl;
import ru.medvedev.importer.service.export.service.TableBuilder;

@Component
public class ContactExporterService extends SimpleExporterImpl<ContactEntity> {

    public ContactExporterService(TableBuilder<ContactEntity> tableBuilder) {
        super(tableBuilder);
    }
}
