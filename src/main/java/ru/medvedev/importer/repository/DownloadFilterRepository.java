package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.DownloadFilterEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.DownloadFilter;

import java.util.List;
import java.util.Optional;

@Repository
public interface DownloadFilterRepository extends JpaRepository<DownloadFilterEntity, Long> {

    List<DownloadFilterEntity> findAllByCode(DownloadFilter code);

    @Query("select df from DownloadFilterEntity df where df.code = :code and " +
            "(df.bank is null or df.bank = :bank)")
    List<DownloadFilterEntity> findAllByCodeAndBank(DownloadFilter code, Bank bank);

    @Transactional
    @Modifying
    void deleteByCode(DownloadFilter code);
}
