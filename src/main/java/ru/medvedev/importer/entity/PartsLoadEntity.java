package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.PartsLoadStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "parts_load")
@Entity
@Data
public class PartsLoadEntity {

    @Id
    @SequenceGenerator(name = "partsLoadGen", sequenceName = "parts_load_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "partsLoadGen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "date_from")
    private LocalDateTime dateFrom;

    @Column(name = "date_to")
    private LocalDateTime dateTo;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "count")
    private Integer count;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PartsLoadStatus status;

    @OneToMany(mappedBy = "partLoad")
    private List<WebhookStatisticEntity> webhookStatisticEntityList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
        this.status = PartsLoadStatus.CREATED;
    }
}
