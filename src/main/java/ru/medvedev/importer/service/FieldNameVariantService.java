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
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class FieldNameVariantService {

    private final FieldNameVariantRepository repository;

    public Map<XlsxRequireField, FieldNameVariantDto> getAll() {
        Map<XlsxRequireField, List<FieldNameVariantEntity>> map = repository.findAll().stream()
                .collect(groupingBy(FieldNameVariantEntity::getField));
        Arrays.stream(XlsxRequireField.values())
                .forEach(field -> map.putIfAbsent(field, Collections.emptyList()));
        return map.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> {
                    FieldNameVariantDto dto = new FieldNameVariantDto();
                    dto.setField(entry.getKey());
                    dto.setNames(entry.getValue().stream()
                            .filter(name -> isNotBlank(name.getName()))
                            .map(FieldNameVariantEntity::getName).collect(Collectors.toSet()));
                    dto.setRequired(entry.getValue().stream()
                            .anyMatch(FieldNameVariantEntity::isRequired));
                    return dto;
                }));
    }

    public FieldNameVariantDto getByField(XlsxRequireField field) {
        FieldNameVariantDto dto = new FieldNameVariantDto();
        List<FieldNameVariantEntity> entities = repository.findAllByField(field);
        dto.setNames(entities.stream()
                .filter(name -> isNotBlank(name.getName()))
                .map(FieldNameVariantEntity::getName)
                .collect(Collectors.toSet()));
        dto.setRequired(entities.stream()
                .anyMatch(FieldNameVariantEntity::isRequired));
        return dto;
    }

    @Transactional
    public void create(List<FieldNameVariantDto> input) {
        repository.deleteAll();
        List<FieldNameVariantEntity> entities = input.stream()
                .flatMap(field -> {
                    List<FieldNameVariantEntity> fnvList = field.getNames().stream()
                            .map(name -> {
                                FieldNameVariantEntity entity = new FieldNameVariantEntity();
                                entity.setField(field.getField());
                                entity.setName(name.replace("\t", ""));
                                entity.setRequired(field.isRequired());
                                return entity;
                            }).collect(Collectors.toList());
                    if (fnvList.isEmpty()) {
                        FieldNameVariantEntity entity = new FieldNameVariantEntity();
                        entity.setField(field.getField());
                        entity.setRequired(field.isRequired());
                        fnvList.add(entity);
                    }
                    return fnvList.stream();
                }).collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
