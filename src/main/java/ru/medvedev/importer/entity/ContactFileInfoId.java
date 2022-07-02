package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class ContactFileInfoId implements Serializable {

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "contact_id")
    private Long contactId;

    public static ContactFileInfoId of(Long fileId, Long contactId) {
        ContactFileInfoId id = new ContactFileInfoId();
        id.setContactId(contactId);
        id.setFileId(fileId);
        return id;
    }
}
