package ru.medvedev.importer.entity;

import lombok.Data;
import lombok.ToString;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ContactStatus status;

    @Column(name = "trash_columns")
    private String trashColumns;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "webhook_status_id")
    private WebhookStatusEntity webhookStatus;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    private List<ContactFileInfoEntity> contactFileInfoEntityList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }

    public ContactEntity getClone() {
        ContactEntity entity = new ContactEntity();
        entity.setOrgName(this.orgName);
        entity.setName(this.name);
        entity.setSurname(this.surname);
        entity.setMiddleName(this.middleName);
        entity.setPhone(this.phone);
        entity.setInn(this.inn);
        entity.setOgrn(this.ogrn);
        entity.setRegion(this.region);
        entity.setCity(this.city);
        entity.setStatus(this.status);
        entity.setTrashColumns(this.trashColumns);
        entity.setBank(this.bank);
        return entity;
    }
}
