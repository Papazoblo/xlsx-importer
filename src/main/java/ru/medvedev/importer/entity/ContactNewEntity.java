package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contact_new")
@Data
public class ContactNewEntity implements Cloneable {

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

    @Column(name = "create_at")
    private LocalDateTime createAt;


    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }

    @Override
    public ContactNewEntity clone() {
        ContactNewEntity entity = new ContactNewEntity();
        entity.setOrgName(this.orgName);
        entity.setName(this.name);
        entity.setSurname(this.surname);
        entity.setMiddleName(this.middleName);
        entity.setPhone(this.phone);
        entity.setInn(this.inn);
        entity.setOgrn(this.ogrn);
        entity.setRegion(this.region);
        entity.setCity(this.city);
        return entity;
    }
}
