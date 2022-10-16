package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.enums.FileInfoBankStatus;

import java.util.Optional;

@Repository
public interface FileInfoBankRepository extends JpaRepository<FileInfoBankEntity, Long> {

    Optional<FileInfoBankEntity> findFirstByDownloadStatus(FileInfoBankStatus status);

    @Modifying
    @Transactional
    @Query("update FileInfoBankEntity fib " +
            "set fib.downloadStatus = :status " +
            "where fib.id = :id")
    void updateStatusById(FileInfoBankStatus status, Long id);
}
