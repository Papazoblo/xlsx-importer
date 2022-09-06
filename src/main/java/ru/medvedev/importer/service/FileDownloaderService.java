package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

@Service
@RequiredArgsConstructor
public class FileDownloaderService {

    private final TelegramPollingService telegramPollingService;

    public FileInfoEntity download(Long fileId) {
        return telegramPollingService.download(fileId);
    }
}
