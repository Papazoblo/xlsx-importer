package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.AutoLinkXlsxFieldEntity;
import ru.medvedev.importer.entity.BankCountSendedRequestEntity;
import ru.medvedev.importer.entity.id.BankCountSendedRequestId;

@Repository
public interface BankCountSendedRequestRepository extends JpaRepository<BankCountSendedRequestEntity, BankCountSendedRequestId> {

}
