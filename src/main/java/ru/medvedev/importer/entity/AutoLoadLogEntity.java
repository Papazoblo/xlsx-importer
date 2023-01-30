package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.AutoLoadPeriod;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "auto_load_log")
@Data
public class AutoLoadLogEntity {

    @Id
    @SequenceGenerator(sequenceName = "auto_load_log_seq_id", name = "autoLoadLogSeqId", allocationSize = 1)
    @GeneratedValue(generator = "autoLoadLogSeqId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_load_id")
    private AutoLoadEntity autoLoad;

    @Column(name = "period")
    @Enumerated(EnumType.STRING)
    private AutoLoadPeriod period;

    @Column(name = "filter")
    private String filter;

    @Column(name = "last_load_date")
    private LocalDate lastLoad;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "enabled")
    private Boolean enable;
}
