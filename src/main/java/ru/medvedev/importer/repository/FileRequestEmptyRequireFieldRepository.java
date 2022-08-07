package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.FileRequestEmptyRequireFieldEntity;

import java.util.Optional;

@Repository
public interface FileRequestEmptyRequireFieldRepository extends JpaRepository<FileRequestEmptyRequireFieldEntity, Long> {

    Optional<FileRequestEmptyRequireFieldEntity> findByFileInfoEntityAndHaveAnswerIsFalse(FileInfoEntity fileInfo);
}
