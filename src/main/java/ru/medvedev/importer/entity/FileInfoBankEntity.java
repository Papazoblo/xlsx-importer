package ru.medvedev.importer.entity;

import lombok.Data;
import lombok.ToString;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.FileInfoBankStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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

    @OneToMany(mappedBy = "fileInfoBank", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ContactDownloadInfoEntity> contactDownloadList = new ArrayList<>();

    @OneToMany(mappedBy = "fileInfoBank", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CheckLeadInfoEntity> checkLeadList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (projectId == null) {
            projectId = -1L;
        }
        downloadStatus = FileInfoBankStatus.NEW;
    }
}
