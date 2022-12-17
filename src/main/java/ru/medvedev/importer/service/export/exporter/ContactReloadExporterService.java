package ru.medvedev.importer.service.export.exporter;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.dto.ContactReloadDto;
import ru.medvedev.importer.service.export.service.SimpleExporterImpl;
import ru.medvedev.importer.service.export.service.TableBuilder;

@Component
public class ContactReloadExporterService extends SimpleExporterImpl<ContactReloadDto> {

    public ContactReloadExporterService(TableBuilder<ContactReloadDto> tableBuilder) {
        super(tableBuilder);
    }
}
