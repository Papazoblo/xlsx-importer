package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ContactDownloadStatisticProjection;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.FileInfoBankStatus;
import ru.medvedev.importer.repository.FileInfoBankRepository;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

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

    public Map<Bank, Integer> getDownloadStatistic(Long fileId) {
        return repository.getDownloadStatistic(fileId).stream()
                .filter(item -> item.getStatus() == ContactStatus.DOWNLOADED)
                .collect(toMap(ContactDownloadStatisticProjection::getBank,
                        ContactDownloadStatisticProjection::getCount));
    }

    public void updateDownloadStatus(FileInfoBankStatus status, Long id) {
        repository.updateStatusById(status, id);
    }
}
