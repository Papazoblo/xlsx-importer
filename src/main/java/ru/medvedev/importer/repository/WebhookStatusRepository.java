package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.WebhookStatusEntity;

import java.util.Optional;

@Repository
public interface WebhookStatusRepository extends JpaRepository<WebhookStatusEntity, Long> {

    Optional<WebhookStatusEntity> findByName(String name);

    boolean existsByName(String name);
}
