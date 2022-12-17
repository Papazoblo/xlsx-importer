package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ContactBankActualityEntity;

@Repository
public interface ContactBankActualityRepository extends JpaRepository<ContactBankActualityEntity, Long> {
}
