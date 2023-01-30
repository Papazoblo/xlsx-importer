package ru.medvedev.importer.entity;

import lombok.Data;
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
    @Column(name = "name")
    private DownloadFilter name;

    @Column(name = "filter")
    private String filter;
}
