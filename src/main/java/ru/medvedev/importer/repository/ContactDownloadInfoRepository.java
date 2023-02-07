package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.ContactDownloadInfoEntity;

import java.util.Collection;
import java.util.Collections;

@Repository
public interface ContactDownloadInfoRepository extends JpaRepository<ContactDownloadInfoEntity, Long> {

    @Modifying
    @Transactional
    void deleteAllByFileInfoBankIdIn(Collection<Long> ids);
}
