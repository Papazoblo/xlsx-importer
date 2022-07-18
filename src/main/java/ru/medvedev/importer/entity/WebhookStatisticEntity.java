package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.WebhookStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_statistic")
@Data
public class WebhookStatisticEntity {

    @Id
    @SequenceGenerator(name = "webhookStatisticGen", sequenceName = "webhook_statistic_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "webhookStatisticGen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "inn")
    private String inn;

    @ManyToOne
    @JoinColumn(name = "webhook_status_id")
    private WebhookSuccessStatusEntity successStatus;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
    }
}
