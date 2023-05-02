package ru.medvedev.importer.entity.id;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Data
public class BankCountSendedRequestId implements Serializable {

    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;

    public static BankCountSendedRequestId currentId(Bank bank) {
        BankCountSendedRequestId id = new BankCountSendedRequestId();
        id.setBank(bank);
        id.setCreateAt(LocalDate.now());
        return id;
    }
}
