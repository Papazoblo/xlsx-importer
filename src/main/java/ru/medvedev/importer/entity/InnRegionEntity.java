package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "inn_region")
@Data
public class InnRegionEntity {

    @Id
    @SequenceGenerator(name = "inn_region_id_gen", sequenceName = "inn_region_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "inn_region_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;
}
