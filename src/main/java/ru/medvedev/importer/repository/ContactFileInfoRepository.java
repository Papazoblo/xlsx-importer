package ru.medvedev.importer.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ContactFileInfoEntity;
import ru.medvedev.importer.entity.ContactFileInfoId;

import java.util.List;
import java.util.Set;

@Repository
public interface ContactFileInfoRepository extends JpaRepository<ContactFileInfoEntity, ContactFileInfoId> {

    @Query("select cfi from ContactFileInfoEntity cfi where cfi.id.contactId in :contactIds")
    List<ContactFileInfoEntity> findByContactId(@Param("contactIds") Set<Long> contactIds);
}
