package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.RequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
@Table(name = "opening_request")
public class RequestEntity {

    @Id
    @SequenceGenerator(name = "openingRequestSeqIdGen", sequenceName = "opening_request_seq_id",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "openingRequestSeqIdGen")
    private Long id;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne
    @JoinColumn(name = "file_info_bank_id")
    private FileInfoBankEntity fileInfoBank;

    @JoinColumn(name = "opening_request_id")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ContactEntity> contacts = new ArrayList<>();

    @Column(name = "last_check")
    public LocalDateTime lastCheck;

    @Column(name = "retry_request_count")
    public Integer retryRequestCount;

    @PrePersist
    public void prePersist() {
        this.lastCheck = LocalDateTime.now();
        if (status == null) {
            this.status = RequestStatus.CREATING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastCheck = LocalDateTime.now();
    }

    public Integer incRetryRequestCount() {
        this.retryRequestCount = (Optional.ofNullable(retryRequestCount).map(value -> value + 1).orElse(1));
        return retryRequestCount;
    }
}
