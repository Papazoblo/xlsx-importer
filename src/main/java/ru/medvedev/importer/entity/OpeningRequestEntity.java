package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.OpeningRequestStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "opening_request")
public class OpeningRequestEntity {

    @Id
    @SequenceGenerator(name = "openingRequestSeqIdGen", sequenceName = "opening_request_seq_id",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "openingRequestSeqIdGen")
    private Long id;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OpeningRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "file_info_bank_id")
    private FileInfoBankEntity fileInfoBank;

    @JoinColumn(name = "opening_request_id")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ContactEntity> contacts = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.status = OpeningRequestStatus.IN_QUEUE;
    }

}
