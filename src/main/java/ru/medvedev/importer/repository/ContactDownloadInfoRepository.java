package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ContactDownloadInfoEntity;

@Repository
public interface ContactDownloadInfoRepository extends JpaRepository<ContactDownloadInfoEntity, Long> {
}
