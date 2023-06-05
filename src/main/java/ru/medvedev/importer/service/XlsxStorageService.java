package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.DownloadAction;
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

    public FileInfoEntity upload(DownloadAction downloadAction, MultipartFile file) throws IOException {
        if (isBlank(file.getOriginalFilename()) || !file.getOriginalFilename().contains(".xlsx")
                || !file.getOriginalFilename().contains(".xls")) {
            throw new BadRequestException("Неверный формат файла");
        }
        return saveFile(downloadAction, file);
    }

    private FileInfoEntity saveFile(DownloadAction downloadAction, MultipartFile file) throws IOException {
        File newFile = new File(uploadDir + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename());
        FileUtils.writeByteArrayToFile(newFile, file.getBytes());
        return fileInfoService.create(downloadAction, file, scanningChatId, newFile, FileSource.UI);
    }
}
