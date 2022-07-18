package ru.medvedev.importer.entity;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user_permission")
@Data
public class UserPermissionEntity {

    @EmbeddedId
    private UserPermissionId id;
}
