package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.ContactStatus;

import javax.persistence.*;

@Entity
@Table(name = "contact")
@Data
public class ContactEntity {

    @Id
    @SequenceGenerator(name = "contact_seq_gen", sequenceName = "contact_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "contact_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "fio")
    private String fio;

    @Column(name = "phone")
    private String phone;

    @Column(name = "inn")
    private String inn;

    @Column(name = "ogrn")
    private String ogrn;

    @Column(name = "region")
    private String region;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ContactStatus status;

    @Column(name = "create_at")
    private String createAt;
}
