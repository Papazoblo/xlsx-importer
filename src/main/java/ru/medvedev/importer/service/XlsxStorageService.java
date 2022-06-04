package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.config.XlsxStorage;
import ru.medvedev.importer.exception.BadRequestException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
public class XlsxStorageService {

    private final XlsxStorage storage;

    @EventListener(ApplicationReadyEvent.class)
    public void checkExistXlsx() {
        checkIsExist();
    }

    public void upload(MultipartFile file) throws IOException {
        if (isBlank(file.getOriginalFilename()) || !file.getOriginalFilename().contains(".xlsx")) {
            throw new BadRequestException("Неверный формат файла");
        }
        deleteIfExist();
        writeToFile(file);
    }

    private void writeToFile(MultipartFile file) throws IOException {
        FileUtils.writeByteArrayToFile(new File(file.getOriginalFilename()), file.getBytes());
        storage.setFileName(file.getName());
    }

    public void deleteIfExist() {
        if (storage.isExist()) {
            File root = new File(".");
            Arrays.stream(root.listFiles())
                    .filter(file -> file.getName().contains(".xlsx"))
                    .forEach(file -> file.delete());
        }
    }

    private void checkIsExist() {
        File root = new File(".");
        Arrays.stream(root.listFiles()).filter(file -> file.getName().contains(".xlsx"))
                .findFirst().ifPresent(file -> {
            storage.setFileName(file.getName());
        });
    }
}
