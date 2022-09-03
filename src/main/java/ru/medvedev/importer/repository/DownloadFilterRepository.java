package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.DownloadFilterEntity;
import ru.medvedev.importer.enums.DownloadFilter;

import java.util.Optional;

@Repository
public interface DownloadFilterRepository extends JpaRepository<DownloadFilterEntity, Long> {

    Optional<DownloadFilterEntity> findByName(DownloadFilter name);

    @Transactional
    @Modifying
    void deleteByName(DownloadFilter name);
}
