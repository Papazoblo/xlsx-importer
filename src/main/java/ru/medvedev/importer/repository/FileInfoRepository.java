package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.FileInfoEntity;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfoEntity, Long> {

    boolean existsByHashAndDeletedIsFalse(String hash);
}
