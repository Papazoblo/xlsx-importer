package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.entity.id.BankCountSendedRequestId;
import ru.medvedev.importer.enums.AutoLoadPeriod;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_count_sended_request")
@Data
public class BankCountSendedRequestEntity {

    @EmbeddedId
    private BankCountSendedRequestId id;

    @Column(name = "crete_request")
    private long createRequestCount;

    @Column(name = "result_create_request")
    private long resultCreateRequestCount;

    @Column(name = "check_request")
    private long checkRequestCount;

    @Column(name = "result_check_request")
    private long resultCheckRequestCount;

    @PrePersist
    public void prePersist() {
        this.id.setCreateAt(LocalDate.now());
    }
}
