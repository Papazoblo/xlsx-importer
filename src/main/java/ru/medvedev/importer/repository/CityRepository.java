package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.CityEntity;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {

    Optional<CityEntity> findByName(String name);
}
