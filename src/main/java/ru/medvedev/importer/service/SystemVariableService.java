package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.SystemVariableEntity;
import ru.medvedev.importer.enums.SystemVariable;
import ru.medvedev.importer.repository.SystemVariableRepository;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;


@Service
@RequiredArgsConstructor
public class SystemVariableService {

    private final SystemVariableRepository repository;

    public Optional<SystemVariableEntity> getByCode(SystemVariable code) {
        return repository.findById(code);
    }

    public Map<SystemVariable, String> getAllMap() {
        return repository.findAll().stream()
                .collect(toMap(SystemVariableEntity::getName, SystemVariableEntity::getValue));
    }

    public void save(Map<SystemVariable, String> variables) {
        variables.forEach(this::save);
    }

    public void save(SystemVariable code, String value) {
        repository.save(getByCode(code).map(entity -> {
            entity.setValue(value);
            return entity;
        }).orElseGet(() -> {
            SystemVariableEntity entity = new SystemVariableEntity();
            entity.setName(code);
            entity.setValue(value);
            return entity;
        }));
    }
}
