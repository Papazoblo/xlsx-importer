package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contact_new")
@Data
public class ContactNewEntity {

    @Id
    @SequenceGenerator(name = "contact_new_seq_gen", sequenceName = "contact_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "contact_new_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "inn")
    private String inn;

    @Column(name = "ogrn")
    private String ogrn;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @OneToMany(mappedBy = "contact", fetch = FetchType.LAZY)
    private List<ContactBankActualityEntity> actualityList = new ArrayList<>();

    @Column(name = "file_info_id")
    private Long fileInfoId;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "alfa_create_at")
    private LocalDateTime alfaCreateAt;


    @PrePersist
    public void prePersist() {
        if (createAt == null) {
            createAt = LocalDateTime.now();
        }
    }

    public static ContactNewEntity of(ContactNewEntity source) {
        ContactNewEntity entity = new ContactNewEntity();
        entity.setOrgName(source.orgName);
        entity.setName(source.name);
        entity.setSurname(source.surname);
        entity.setMiddleName(source.middleName);
        entity.setPhone(source.phone);
        entity.setInn(source.inn);
        entity.setOgrn(source.ogrn);
        entity.setRegion(source.region);
        entity.setCity(source.city);
        return entity;
    }
}
