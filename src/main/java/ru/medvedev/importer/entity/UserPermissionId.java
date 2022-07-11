package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.Permission;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
@Data
public class UserPermissionId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "permission_code")
    @Enumerated(EnumType.STRING)
    private Permission permission;

    public static UserPermissionId of(Long userId, Permission permission) {
        UserPermissionId id = new UserPermissionId();
        id.setPermission(permission);
        id.setUserId(userId);
        return id;
    }
}
