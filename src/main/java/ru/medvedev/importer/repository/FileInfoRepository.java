package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.FileStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, Long> {

    Optional<FileInfoEntity> findFirstByStatusOrderByCreateAt(FileStatus status);

    @Query(value = "select chat_id from file_info group by chat_id", nativeQuery = true)
    List<Long> getAllChatId();

    @Modifying
    @Transactional
    @Query("update FileInfoEntity fi set fi.status = :status where fi.id = :id")
    void changeStatus(@Param("id") Long id, @Param("status") FileStatus status);

    boolean existsByHashAndStatus(String hash, FileStatus status);

    boolean existsByStatus(FileStatus status);
}
