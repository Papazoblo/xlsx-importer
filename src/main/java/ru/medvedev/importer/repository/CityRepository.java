package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.CityEntity;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {

    @Query("select c from CityEntity c where lower(c.name) = lower(:name)")
    Optional<CityEntity> findByName(@Param("name") String name);
}
