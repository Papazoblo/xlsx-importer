package ru.medvedev.importer.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckDirectoryService {

    @Value("${directory.upload-dir}")
    private String uploadDir;

    @EventListener(ApplicationReadyEvent.class)
    public void launchCheckExistFolders() {
        log.info("*** Launch check exists system folders");
        File folder = new File(uploadDir);
        if (!folder.exists() && !folder.mkdirs()) {
            log.info("*** Couldn't create folder " + uploadDir);
            throw new IllegalStateException("*** Couldn't create folder: " + uploadDir);
        }
    }
}
