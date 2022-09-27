package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.ProjectNumberEntity;
import ru.medvedev.importer.enums.Bank;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProjectNumberRepository extends JpaRepository<ProjectNumberEntity, Long> {

    boolean existsByDateAndBank(LocalDate date, Bank bank);

    Optional<ProjectNumberEntity> findByBankAndDate(Bank bank, LocalDate date);
}
