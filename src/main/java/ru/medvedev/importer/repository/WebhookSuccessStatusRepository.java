package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.WebhookType;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookSuccessStatusRepository extends JpaRepository<WebhookSuccessStatusEntity, Long>,
        JpaSpecificationExecutor<WebhookSuccessStatusEntity> {


    @Query("select w from WebhookSuccessStatusEntity w " +
            "where w.webhookId = :webhookId " +
            "and w.type in (ru.medvedev.importer.enums.WebhookType.SUCCESS, ru.medvedev.importer.enums.WebhookType.ERROR)")
    List<WebhookSuccessStatusEntity> findAllByBankAndWebhookId(Long webhookId);

    @Query("select s from WebhookSuccessStatusEntity s " +
            "where s.type = ru.medvedev.importer.enums.WebhookType.CREATE_REQUEST " +
            "and s.webhookStatusEntity.name = :name")
    Optional<WebhookSuccessStatusEntity> findByNameAndType(String name);

    List<WebhookSuccessStatusEntity> findAllByTypeInOrderByWebhookId(List<WebhookType> typeList);
}
