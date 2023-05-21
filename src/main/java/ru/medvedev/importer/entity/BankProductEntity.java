package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.BankProduct;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.*;

@Data
@Entity
@Table(name = "bank_product")
public class BankProductEntity {

    @Id
    @SequenceGenerator(name = "bank_product_seq_id_gen", sequenceName = "bank_product_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "bank_product_seq_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "code")
    @Enumerated(EnumType.STRING)
    private BankProduct code;

    @Column(name = "title")
    private String title;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;
}
