package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ProjectNumberEntity;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProjectNumberRepository extends JpaRepository<ProjectNumberEntity, Long> {

    boolean existsByDate(LocalDate date);

    Optional<ProjectNumberEntity> findByDate(LocalDate date);
}
