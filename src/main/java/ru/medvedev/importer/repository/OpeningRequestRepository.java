package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.OpeningRequestEntity;
import ru.medvedev.importer.enums.OpeningRequestStatus;

import java.util.Optional;

@Repository
public interface OpeningRequestRepository extends JpaRepository<OpeningRequestEntity, Long> {

    Optional<OpeningRequestEntity> findFirstByStatus(OpeningRequestStatus status);

    @Modifying
    @Transactional
    @Query("update OpeningRequestEntity r set r.status = :newStatus where r.id = :id")
    void updateStatusById(Long id, OpeningRequestStatus newStatus);
}
