package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.dto.DailyContactStatistic;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.WebhookStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookStatisticRepository extends JpaRepository<WebhookStatisticEntity, Long> {

    @Query(value = "select webhook_statistic.status as status, count(*) as count, webhook_statistic.bank_name as bank\n" +
            "from webhook_statistic\n" +
            "where create_at >= :dateFrom\n" +
            "  and create_at <= :dateTo\n" +
            "group by status, webhook_statistic.bank_name", nativeQuery = true)
    List<DailyContactStatistic> findByCreateAtLessThanEqualAndCreateAtGreaterThan(LocalDateTime dateTo,
                                                                                  LocalDateTime dateFrom);

    List<WebhookStatisticEntity> findAllByInnAndStatus(String inn, WebhookStatus status);

    List<WebhookStatisticEntity> findAllByStatus(WebhookStatus status);
}
