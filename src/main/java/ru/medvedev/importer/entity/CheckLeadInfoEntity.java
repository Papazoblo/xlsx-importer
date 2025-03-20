package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.ContactStatus;

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
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;

@Table(name = "lead_check_info")
@Entity
@Data
public class CheckLeadInfoEntity {
    @Id
    @SequenceGenerator(name = "lead_check_info_GenId", sequenceName = "lead_check_info_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_check_info_GenId")
    private Long id;

    @Column(name = "inn")
    private String inn;

    @Column(name = "file_info_bank_id")
    private Long fileInfoBankId;

    @JoinColumn(name = "file_info_bank_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private FileInfoBankEntity fileInfoBank;

    @Column(name = "file_info_id")
    private Long fileInfoId;

    @JoinColumn(name = "file_info_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private FileInfoEntity fileInfo;

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
        this.checkStatus = Optional.ofNullable(checkStatus).orElse(ContactStatus.IN_CHECK);
    }
}
