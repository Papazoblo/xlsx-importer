package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "contact_file_info")
@Data
public class ContactFileInfoEntity {

    @EmbeddedId
    private ContactFileInfoId id;

    @Column(name = "original")
    private boolean original;
}
