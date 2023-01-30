package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.AutoLoadPeriod;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auto_load")
@Data
public class AutoLoadEntity {

    @Id
    @SequenceGenerator(sequenceName = "auto_load_seq_id", name = "autoLoadSeqId", allocationSize = 1)
    @GeneratedValue(generator = "autoLoadSeqId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "interval")
    private Integer interval;

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
    private Boolean enabled;

    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(mappedBy = "autoLoad", fetch = FetchType.LAZY)
    private List<AutoLoadLogEntity> logs = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        enabled = true;
        deleted = false;
        createDate = LocalDate.now();
    }
}
