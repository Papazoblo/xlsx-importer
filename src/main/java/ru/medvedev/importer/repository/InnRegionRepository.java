package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.InnRegionEntity;

import java.util.Optional;

@Repository
public interface InnRegionRepository extends JpaRepository<InnRegionEntity, Long> {

    Optional<InnRegionEntity> findByCode(String code);
}
