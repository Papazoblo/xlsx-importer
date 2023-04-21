package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "first_load_lead")
@Data
public class FirstLoadLeadEntity {

    @Id
    private String inn;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @PrePersist
    public void prePersists() {
        createDate = LocalDateTime.now();
    }
}
