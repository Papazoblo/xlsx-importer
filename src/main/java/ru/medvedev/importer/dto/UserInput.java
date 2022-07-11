package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.Permission;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserInput {

    private String fio;
    private String login;
    private String password;
    private List<Permission> permissions = new ArrayList<>();
}
