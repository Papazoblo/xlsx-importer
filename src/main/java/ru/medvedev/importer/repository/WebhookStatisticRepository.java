package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.WebhookStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookStatisticRepository extends JpaRepository<WebhookStatisticEntity, Long> {

    List<WebhookStatisticEntity> findByCreateAtLessThanEqualAndCreateAtGreaterThan(LocalDateTime dateTo,
                                                                                   LocalDateTime dateFrom);

    List<WebhookStatisticEntity> findAllByInnAndStatus(String inn, WebhookStatus status);

    List<WebhookStatisticEntity> findAllByStatus(WebhookStatus status);
}
