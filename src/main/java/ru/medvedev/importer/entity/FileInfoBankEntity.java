package ru.medvedev.importer.entity;

import lombok.Data;
import lombok.ToString;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.FileInfoBankStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_info_bank")
@Data
public class FileInfoBankEntity {

    @Id
    @SequenceGenerator(sequenceName = "file_info_bank_seq_id", name = "fileInfoBankSeqId", allocationSize = 1)
    @GeneratedValue(generator = "fileInfoBankSeqId", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_info_id")
    private FileInfoEntity fileInfo;

    @Column(name = "file_info_id", insertable = false, updatable = false)
    private Long fileInfoId;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "download_status")
    @Enumerated(EnumType.STRING)
    private FileInfoBankStatus downloadStatus;

    @OneToMany(mappedBy = "fileInfoBankDownload", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ContactEntity> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "fileInfoBank", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<OpeningRequestEntity> openingRequests = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (projectId == null) {
            projectId = -1L;
        }
        downloadStatus = FileInfoBankStatus.NEW;
    }
}
