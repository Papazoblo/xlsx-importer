package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.entity.FieldNameVariantEntity;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.repository.FieldNameVariantRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class FieldNameVariantService {

    private final FieldNameVariantRepository repository;

    public Map<XlsxRequireField, List<String>> getAll() {
        Map<XlsxRequireField, List<FieldNameVariantEntity>> map = repository.findAll().stream()
                .collect(groupingBy(FieldNameVariantEntity::getField));
        Arrays.stream(XlsxRequireField.values())
                .forEach(field -> map.putIfAbsent(field, Collections.emptyList()));
        return map.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(FieldNameVariantEntity::getName).collect(Collectors.toList())));
    }

    public List<String> getByField(XlsxRequireField field) {
        return repository.findAllByField(field).stream()
                .map(FieldNameVariantEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void create(List<FieldNameVariantDto> input) {
        repository.deleteAll();
        List<FieldNameVariantEntity> entities = input.stream()
                .flatMap(field -> field.getNames().stream()
                        .map(name -> {
                            FieldNameVariantEntity entity = new FieldNameVariantEntity();
                            entity.setField(field.getField());
                            entity.setName(name);
                            return entity;
                        }).collect(Collectors.toList()).stream()
                ).collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
