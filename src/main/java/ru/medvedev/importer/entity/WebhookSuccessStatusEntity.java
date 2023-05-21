package ru.medvedev.importer.entity;

import lombok.Data;
import lombok.ToString;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webhook_success_status")
@Data
public class WebhookSuccessStatusEntity implements Cloneable {

    @Id
    @SequenceGenerator(name = "webhook_success_status_id_gen", sequenceName = "webhook_success_status_seq_id",
            allocationSize = 1)
    @GeneratedValue(generator = "webhook_success_status_id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "webhook_id")
    private Long webhookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_id", insertable = false, updatable = false)
    private WebhookStatusEntity webhookStatusEntity;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private WebhookType type;

    @Column(name = "bank_name")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @ManyToMany
    @JoinTable(name = "webhook_success_status_product",
            joinColumns = @JoinColumn(name = "webhook_success_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<BankProductEntity> products = new ArrayList<>();

    @Override
    public WebhookSuccessStatusEntity clone() throws CloneNotSupportedException {
        return (WebhookSuccessStatusEntity) super.clone();
    }
}
