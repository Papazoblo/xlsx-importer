package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;

import javax.persistence.*;
import java.util.Optional;

@Data
@Table(name = "contact_bank_actuality")
@Entity
public class ContactBankActualityEntity {

    @Id
    @SequenceGenerator(name = "contact_bank_actuality_gen", sequenceName = "contact_bank_actuality_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "contact_bank_actuality_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "webhook_status_id")
    private Long webhookStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_status_id", updatable = false, insertable = false)
    private WebhookStatusEntity webhookStatusEntity;

    @Column(name = "actuality")
    @Enumerated(EnumType.STRING)
    private ContactActuality actuality = ContactActuality.NEW;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "contact_id")
    private Long contactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    private ContactNewEntity contact;

    public Integer incErrorCount() {
        this.errorCount = Optional.ofNullable(this.errorCount).orElse(0) + 1;
        return this.errorCount;
    }
}
