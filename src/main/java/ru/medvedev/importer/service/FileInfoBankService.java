package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.enums.FileInfoBankStatus;
import ru.medvedev.importer.repository.FileInfoBankRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileInfoBankService {

    private final FileInfoBankRepository repository;

    public FileInfoBankEntity save(FileInfoBankEntity entity) {
        return repository.save(entity);
    }

    public Optional<FileInfoBankEntity> getByDownloadStatus(FileInfoBankStatus status) {
        return repository.findFirstByDownloadStatus(status);
    }

    public void updateDownloadStatus(FileInfoBankStatus status, Long id) {
        repository.updateStatusById(status, id);
    }
}
