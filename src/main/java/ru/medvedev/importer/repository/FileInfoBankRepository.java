package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.ContactDownloadStatisticProjection;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.enums.FileInfoBankStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoBankRepository extends JpaRepository<FileInfoBankEntity, Long> {

    Optional<FileInfoBankEntity> findFirstByDownloadStatus(FileInfoBankStatus status);

    @Query(value = "select c.status as status, c.bank_name as bank, count(c.id) as count\n" +
            "from file_info_bank fib\n" +
            "join contact c on c.file_info_bank_id = fib.id\n" +
            "where fib.file_info_id = :fileId\n" +
            "group by (c.status, c.bank_name)", nativeQuery = true)
    List<ContactDownloadStatisticProjection> getDownloadStatistic(Long fileId);

    @Modifying
    @Transactional
    @Query("update FileInfoBankEntity fib " +
            "set fib.downloadStatus = :status " +
            "where fib.id = :id")
    void updateStatusById(FileInfoBankStatus status, Long id);
}
