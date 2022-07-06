package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "permission")
@Data
public class PermissionEntity {

    @Id
    @SequenceGenerator(name = "permission_seq_id_gen", sequenceName = "permission_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "permission_seq_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;
}
