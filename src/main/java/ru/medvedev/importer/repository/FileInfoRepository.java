package ru.medvedev.importer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.FileStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, Long> {

    Optional<FileInfoEntity> findFirstByStatusAndProcessingStepOrderByCreateAt(FileStatus status, FileProcessingStep step);

    Page<FileInfoEntity> findAllByDeletedIsFalse(Pageable pageable);

    @Query(value = "select chat_id from file_info group by chat_id", nativeQuery = true)
    List<Long> getAllChatId();

    @Query("select fib " +
            "from FileInfoEntity fi " +
            "join FileInfoBankEntity fib on fib.fileInfoId = fi.id " +
            "where fi.source = :source " +
            "and fi.status in :statuses " +
            "and fib.projectId is not null " +
            "order by fi.id DESC")
    List<FileInfoBankEntity> getLastUiProjectCode(FileSource source, List<FileStatus> statuses);

    @Modifying
    @Transactional
    @Query("update FileInfoEntity fi set fi.status = :status where fi.id = :id")
    void changeStatus(@Param("id") Long id, @Param("status") FileStatus status);

    Optional<FileInfoEntity> findByStatusAndSourceAndProcessingStepIn(FileStatus status, FileSource source,
                                                                      List<FileProcessingStep> processingStep);

    Optional<FileInfoEntity> findByStatusAndSource(FileStatus status, FileSource source);

    Optional<FileInfoEntity> findFirstByProcessingStepAndSourceAndStatus(FileProcessingStep processingStep,
                                                                         FileSource source,
                                                                         FileStatus status);

    @Query("select (count(fi) > 0) from FileInfoEntity fi " +
            "where lower(fi.hash) = lower(:hash) and " +
            "fi.status <> :status")
    boolean existsByHashAndStatusNot(String hash, FileStatus status);

    boolean existsByStatus(FileStatus status);
}
