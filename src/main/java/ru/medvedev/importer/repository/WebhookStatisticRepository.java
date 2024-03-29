package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.DailyContactStatistic;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.WebhookStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookStatisticRepository extends JpaRepository<WebhookStatisticEntity, Long> {

    @Query(value = "select webhook_statistic.status as status, count(*) as count, webhook_statistic.bank_name as bank\n" +
            "from webhook_statistic\n" +
            "where create_at >= :dateFrom\n" +
            "  and create_at <= :dateTo\n" +
            "group by status, webhook_statistic.bank_name", nativeQuery = true)
    List<DailyContactStatistic> findByCreateAtLessThanEqualAndCreateAtGreaterThan(LocalDateTime dateTo,
                                                                                  LocalDateTime dateFrom);

    @Modifying
    @Transactional
    @Query("update WebhookStatisticEntity ws set ws.status = :newStatus where ws.id = :id")
    void updateStatus(Long id, WebhookStatus newStatus);

    @Modifying
    @Transactional
    @Query("update WebhookStatisticEntity ws set ws.status = :newStatus, ws.openingRequestId = :requestId " +
            "where ws.id = :id")
    void updateStatusAndRequestId(Long id, WebhookStatus newStatus, String requestId);

    List<WebhookStatisticEntity> findAllByInnAndStatus(String inn, WebhookStatus status);

    Optional<WebhookStatisticEntity> findFirstByStatusOrderByUpdateAtDesc(WebhookStatus status);
}
