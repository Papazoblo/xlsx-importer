package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.SystemVariable;

import java.util.HashMap;
import java.util.Map;

@Data
public class SystemVariableDto {

    private Map<SystemVariable, String> variables = new HashMap<>();
}
