package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ContactFileInfoEntity;
import ru.medvedev.importer.entity.ContactFileInfoId;

@Repository
public interface ContactFileInfoRepository extends JpaRepository<ContactFileInfoEntity, ContactFileInfoId> {
}
