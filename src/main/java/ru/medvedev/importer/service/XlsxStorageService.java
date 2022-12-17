package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.exception.BadRequestException;

import java.io.File;
import java.io.IOException;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
public class XlsxStorageService {

    @Value("${telegram.xlsx-collector.scanningChatId}")
    private Long scanningChatId;
    @Value("${directory.upload-dir}")
    private String uploadDir;

    private final FileInfoService fileInfoService;

    /*@Scheduled(fixedRateString = "${cron.fixed-rate-check-xlsx-exists}")
    public void scheduleCheckExistXlsx() {
        checkIsExist();
    }*/

    /*@EventListener(ApplicationReadyEvent.class)
    public void checkExistXlsx() {
        checkIsExist();
    }*/

    public FileInfoEntity upload(MultipartFile file) throws IOException {
        if (isBlank(file.getOriginalFilename()) || !file.getOriginalFilename().contains(".xlsx")
                || !file.getOriginalFilename().contains(".xls")) {
            throw new BadRequestException("Неверный формат файла");
        }
        //deleteIfExist();
        return saveFile(file);
    }

    private FileInfoEntity saveFile(MultipartFile file) throws IOException {
        File newFile = new File(uploadDir + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
        FileUtils.writeByteArrayToFile(newFile, file.getBytes());
        return fileInfoService.create(file, scanningChatId, newFile, FileSource.UI);
    }/*

    public void deleteIfExist() {
        if (storage.isExist()) {
            File root = new File(".");
            Arrays.stream(root.listFiles())
                    .filter(file -> file.getName().contains(".xlsx") || file.getName().contains(".xls"))
                    .forEach(file -> file.delete());
        }
    }*/

    /*private void checkIsExist() {
        File root = new File(".");
        Arrays.stream(root.listFiles()).filter(file -> file.getName().contains(".xlsx") || file.getName().contains(".xls"))
                .findFirst().ifPresent(file -> {
            storage.setFileName(file.getName());
        });
    }*/
}
