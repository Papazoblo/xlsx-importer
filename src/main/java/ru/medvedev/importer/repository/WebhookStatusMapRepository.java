package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.WebhookStatusMapEntity;
import ru.medvedev.importer.enums.Bank;

import java.util.List;

@Repository
public interface WebhookStatusMapRepository extends JpaRepository<WebhookStatusMapEntity, Long> {

    List<WebhookStatusMapEntity> findAllByBank(Bank bank);

    @Query("select w from WebhookStatusMapEntity w order by w.bank, w.actuality, w.webhookStatus.id, w.errorCount")
    List<WebhookStatusMapEntity> findAllWithSort();
}
