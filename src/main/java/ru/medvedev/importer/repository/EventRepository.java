package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
}
