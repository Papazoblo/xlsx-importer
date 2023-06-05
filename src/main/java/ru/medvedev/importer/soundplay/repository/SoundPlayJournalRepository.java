package ru.medvedev.importer.soundplay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.soundplay.entity.SoundPlayJournalEntity;

import java.util.Optional;

@Repository
public interface SoundPlayJournalRepository extends JpaRepository<SoundPlayJournalEntity, Long> {

    Optional<SoundPlayJournalEntity> findByRequestId(@Param("requestId") String id);
}
