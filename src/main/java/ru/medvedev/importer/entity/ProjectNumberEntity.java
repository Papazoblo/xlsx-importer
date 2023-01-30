package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_number")
@Data
public class ProjectNumberEntity {

    @Id
    @SequenceGenerator(name = "projectNumberSeqId", sequenceName = "project_number_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "projectNumberSeqId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "number")
    private String number;
}
