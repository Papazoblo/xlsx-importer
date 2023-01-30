package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.EnabledScenarioEntity;

import java.util.Optional;

@Repository
public interface EnabledScenarioRepository extends JpaRepository<EnabledScenarioEntity, Long> {

    boolean existsByScenarioId(Long scenarioId);

    Optional<EnabledScenarioEntity> findByScenarioId(Long id);
}
