package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.UserEntity;
import ru.medvedev.importer.enums.Permission;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserDto {

    private Long id;
    private String fio;
    private String login;
    private String createAt;
    private boolean active;
    private String permissions;

    public boolean isHavePermission(String permission) {
        return permissions.contains(permission);
    }

    public static UserDto of(UserEntity entity, List<Permission> permissions) {
        UserDto dto = new UserDto();
        dto.setActive(entity.getActive());
        dto.setFio(entity.getFio());
        dto.setLogin(entity.getLogin());
        dto.setCreateAt(entity.getCreateAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dto.setId(entity.getId());
        dto.setPermissions(permissions.stream()
                .sorted(Comparator.comparing(Permission::getDescription))
                .map(permission -> "- " + permission.getDescription())
                .collect(Collectors.joining("\n"))
        );
        return dto;
    }
}
