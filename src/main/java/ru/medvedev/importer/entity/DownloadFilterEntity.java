package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.DownloadFilter;

import javax.persistence.*;

@Entity
@Table(name = "download_filter")
@Data
public class DownloadFilterEntity {

    @Id
    @SequenceGenerator(name = "downloadFilterGenId", sequenceName = "download_filter_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "downloadFilterGenId")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "code")
    private DownloadFilter code;

    @Column(name = "value")
    private String value;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;
}
