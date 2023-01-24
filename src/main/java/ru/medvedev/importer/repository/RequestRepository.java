package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.RequestStatisticProjection;
import ru.medvedev.importer.entity.RequestEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

    Optional<RequestEntity> findFirstByStatusOrderByLastCheckAsc(RequestStatus status);

    @Query(value = "select r from RequestEntity r " +
            "where r.status = :status and r.fileInfoBank.bank = :bank " +
            "order by r.lastCheck asc ")
    List<RequestEntity> findFirstByStatusAndBank(RequestStatus status,
                                                 Bank bank);

    @Query(value = "select fib.bank as bank, r.status as status, r.count as count\n" +
            "from opening_request r\n" +
            "join file_info_bank fib on r.file_info_bank_id = fib.id\n" +
            "where fib.file_info_id = :fileId\n" +
            "group by (fib.bank, r.status)", nativeQuery = true)
    List<RequestStatisticProjection> getStatisticByFile(Long fileId);

    @Modifying
    @Transactional
    @Query("update RequestEntity r set r.status = :newStatus where r.id = :id")
    void updateStatusById(Long id, RequestStatus newStatus);

    void deleteAllByFileInfoBankIdIn(Set<Long> fibId);
}
