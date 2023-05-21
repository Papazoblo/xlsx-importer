package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.BankProductEntity;
import ru.medvedev.importer.enums.BankProduct;
import ru.medvedev.importer.enums.Bank;

import java.util.List;

@Repository
public interface BankProductRepository extends JpaRepository<BankProductEntity, Long> {

    List<BankProductEntity> findAllByBankAndCodeIn(Bank bank, List<BankProduct> products);
}
