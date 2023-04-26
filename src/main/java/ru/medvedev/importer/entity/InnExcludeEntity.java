package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.entity.id.InnExcludeId;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inn_exclude")
@Data
public class InnExcludeEntity {

    @EmbeddedId
    private InnExcludeId id;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
    }
}
