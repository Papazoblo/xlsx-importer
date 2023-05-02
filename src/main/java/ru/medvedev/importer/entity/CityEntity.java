package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "city")
public class CityEntity {

    @Id
    @SequenceGenerator(name = "citySeqGetId", sequenceName = "city_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "citySeqGetId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "raiffeisen_id")
    private String raiffeisenId;

    @Column(name = "alfa_id")
    private String alfaId;
}
