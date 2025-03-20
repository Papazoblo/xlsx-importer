package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.CheckLeadInfoEntity;

@Repository
public interface CheckLeadInfoRepository extends JpaRepository<CheckLeadInfoEntity, Long> {
}
