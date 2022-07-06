package ru.medvedev.importer.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.ContactStatistic;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.enums.ContactStatus;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

    List<ContactEntity> findAllByInnIn(List<String> inn);

    @Query(value = "select c.id " +
            "from contact c " +
            "         join contact_file_info cfi on c.id = cfi.contact_id " +
            "where cfi.file_id = :fileId " +
            "  and c.inn in (:innList)", nativeQuery = true)
    List<Long> findContactIdByInn(@Param("fileId") Long fileId,
                                  @Param("innList") List<String> innList);

    @Query(value = "select count(id) as count, status\n" +
            "from contact c\n" +
            "join contact_file_info cfi on cfi.contact_id = c.id\n" +
            "where cfi.file_id = :fileId\n" +
            "group by status", nativeQuery = true)
    List<ContactStatistic> getContactStatisticByFileId(@Param("fileId") Long fileId);

    @Modifying
    @Transactional
    @Query("update ContactEntity c set c.status = :status where c.id in :idList")
    void changeContactStatusById(@Param("status") ContactStatus status,
                                 @Param("idList") List<Long> idList);
}
