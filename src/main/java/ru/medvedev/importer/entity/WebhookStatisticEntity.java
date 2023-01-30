package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
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

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "phone")
    private String phone;

    @Column(name = "city")
    private String city;

    @Column(name = "name")
    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "comment_text")
    private String comment;

    @Column(name = "email")
    private String email;

    @Column(name = "error_message")
    private String errorMessage;

    @ManyToOne
    @JoinColumn(name = "webhook_status_id")
    private WebhookSuccessStatusEntity successStatus;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    @Column(name = "opening_id")
    private String openingRequestId;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
        this.updateAt = createAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
