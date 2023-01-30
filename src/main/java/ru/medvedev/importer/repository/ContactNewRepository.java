package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.dto.ContactStatistic;
import ru.medvedev.importer.entity.ContactNewEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactNewRepository extends JpaRepository<ContactNewEntity, Long>,
        JpaSpecificationExecutor<ContactNewEntity> {

    Optional<ContactNewEntity> findFirstByInn(String inn);

    boolean existsByInn(String inn);

    @Query("select c.id from ContactNewEntity c where c.inn in :innList")
    List<Long> findContactIdByInn(List<String> innList);

    @Query(value = "select count(id) as count, check_status\n" +
            "from contact_new c\n" +
            "         join contact_download_info cdi on cdi.contact_id = c.id\n" +
            "         join file_info_bank fib on fib.id = cdi.file_info_bank_id\n" +
            "where fib.file_info_id = :fileId\n" +
            "group by check_status", nativeQuery = true)
    List<ContactStatistic> getContactStatisticByFileId(@Param("fileId") Long fileId);
}
