package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.entity.InnExcludeEntity;
import ru.medvedev.importer.entity.id.InnExcludeId;
import ru.medvedev.importer.enums.Bank;

import java.util.List;

@Repository
public interface InnExcludeRepository extends JpaRepository<InnExcludeEntity, InnExcludeId>,
        JpaSpecificationExecutor<InnExcludeEntity> {

    List<InnExcludeEntity> findAllById_Bank(Bank bank);

    @Modifying
    @Transactional
    void deleteAllById_Bank(Bank bank);

    @Modifying
    @Transactional
    void deleteAllById_BankAndId_InnIn(Bank bank, List<String> inn);

}