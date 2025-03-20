package ru.medvedev.importer.dto.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.medvedev.importer.entity.FileInfoEntity;

@Getter
public class SendResultCheckExportEvent extends ApplicationEvent {

    private final FileInfoEntity fileInfo;

    public SendResultCheckExportEvent(Object source, FileInfoEntity fileInfo) {
        super(source);
        this.fileInfo = fileInfo;
    }
}
