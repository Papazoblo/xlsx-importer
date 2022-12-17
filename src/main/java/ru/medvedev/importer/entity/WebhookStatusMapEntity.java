package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;

import javax.persistence.*;
import java.util.Optional;

@Data
@Table(name = "webhook_status_map")
@Entity
public class WebhookStatusMapEntity {

    @Id
    @SequenceGenerator(name = "webhookStatusMapGenId", sequenceName = "webhook_status_map_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "webhookStatusMapGenId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "from_status_id")
    private Long fromStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_status_id", updatable = false, insertable = false)
    private WebhookStatusEntity webhookStatus;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "actuality")
    @Enumerated(EnumType.STRING)
    private ContactActuality actuality;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "error_count")
    private Integer errorCount;

    @PrePersist
    public void prePersist() {
        this.priority = Optional.ofNullable(priority).orElse(1);
    }
}
