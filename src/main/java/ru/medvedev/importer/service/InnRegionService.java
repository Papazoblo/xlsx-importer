package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.InnRegionEntity;
import ru.medvedev.importer.repository.InnRegionRepository;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class InnRegionService {

    private final InnRegionRepository repository;

    public Map<String, InnRegionEntity> getAllMap() {
        return repository.findAll().stream()
                .collect(toMap(InnRegionEntity::getCode, item -> item));
    }
}
