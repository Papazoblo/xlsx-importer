package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.ContactStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "contact_download_info")
@Entity
@Data
public class ContactDownloadInfoEntity {

    @Id
    @SequenceGenerator(name = "contactDownloadInfoGenId", sequenceName = "contact_download_info_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contactDownloadInfoGenId")
    private Long id;

    @Column(name = "contact_id")
    private Long contactId;

    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ContactNewEntity contact;

    @Column(name = "file_info_bank_id")
    private Long fileInfoBankId;

    @JoinColumn(name = "file_info_bank_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private FileInfoBankEntity fileInfoBank;

    @Column(name = "request_id")
    private Long requestId;

    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RequestEntity request;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "check_status")
    @Enumerated(EnumType.STRING)
    private ContactStatus checkStatus;

    @PrePersist
    public void prePersist() {
        this.createAt = LocalDateTime.now();
        this.checkStatus = ContactStatus.IN_CHECK;
    }
}
