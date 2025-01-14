package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.dto.ContactCountDateProjection;
import ru.medvedev.importer.dto.ContactStatistic;
import ru.medvedev.importer.entity.ContactNewEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactNewRepository extends JpaRepository<ContactNewEntity, Long>,
        JpaSpecificationExecutor<ContactNewEntity> {

    long countByFileInfoId(Long fileInfoId);

    Optional<ContactNewEntity> findFirstByInn(String inn);

    Optional<ContactNewEntity> findFirstByPhoneInOrderByIdDesc(List<String> phone);

    boolean existsByInn(String inn);

    List<ContactNewEntity> findAllByAlfaCreateAtGreaterThanEqualAndAlfaCreateAtLessThanEqualOrderByCreateAt(LocalDateTime dateFrom,
                                                                                                            LocalDateTime dateTo);

    @Query(value = "select count(id) as count, cast(create_at as date) as date\n" +
            "from contact_new\n" +
            "where\n" +
            "    (:dateFrom is null or create_at >= to_date(cast(:dateFrom as text), 'yyyy-mm-dd'))\n" +
            "    and (:dateTo is null or create_at <= to_date(cast(:dateTo as text), 'yyyy-mm-dd'))\n" +
            "group by cast(create_at as date)\n" +
            "order by cast(create_at as date) desc\n" +
            "offset :offset\n" +
            "limit :limit", nativeQuery = true)
    List<ContactCountDateProjection> getCountDateStatistic(@Param("dateFrom") String dateFrom,
                                                           @Param("dateTo") String dateTo,
                                                           @Param("offset") int offset,
                                                           @Param("limit") int limit);

    @Query(value = "select count(*)\n" +
            "from (select cast(create_at as date)\n" +
            "      from contact_new\n" +
            "      group by cast(create_at as date)\n" +
            "      order by create_at desc) result", nativeQuery = true)
    long getTotalRecordCountDateStatistic();

    @Query("select c from ContactNewEntity c where c.inn in :innList")
    List<ContactNewEntity> findAllContactByInn(List<String> innList);

    @Query(value = "select count(id) as count, check_status\n" +
            "from contact_new c\n" +
            "         join contact_download_info cdi on cdi.contact_id = c.id\n" +
            "         join file_info_bank fib on fib.id = cdi.file_info_bank_id\n" +
            "where fib.file_info_id = :fileId\n" +
            "group by check_status", nativeQuery = true)
    List<ContactStatistic> getContactStatisticByFileId(@Param("fileId") Long fileId);
}
