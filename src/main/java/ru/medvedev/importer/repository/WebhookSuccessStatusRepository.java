package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;

import java.util.Optional;

@Repository
public interface WebhookSuccessStatusRepository extends JpaRepository<WebhookSuccessStatusEntity, Long> {

    Optional<WebhookSuccessStatusEntity> findByName(String name);

    boolean existsByName(String name);
}
