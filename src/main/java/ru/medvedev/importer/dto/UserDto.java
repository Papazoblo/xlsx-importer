package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.UserEntity;

@Data
public class UserDto {

    private Long id;
    private String fio;
    private String login;
    private String password;
    private String createAt;
    private boolean active;

    public static UserDto of(UserEntity entity) {
        return new UserDto();
    }
}
