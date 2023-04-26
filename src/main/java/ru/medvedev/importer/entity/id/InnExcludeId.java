package ru.medvedev.importer.entity.id;

import lombok.Data;
import ru.medvedev.importer.enums.Bank;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.security.SecureRandom;

@Embeddable
@Data
public class InnExcludeId implements Serializable {

    @Column(name = "inn")
    private String inn;

    @Column(name = "bank")
    @Enumerated(EnumType.STRING)
    private Bank bank;
}
