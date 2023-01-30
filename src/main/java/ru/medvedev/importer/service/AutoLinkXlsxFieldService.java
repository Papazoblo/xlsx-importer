package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.AutoLinkXlsxFieldDto;
import ru.medvedev.importer.entity.AutoLinkXlsxFieldEntity;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.repository.AutoLinkXlsxFieldRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class AutoLinkXlsxFieldService {

    private final AutoLinkXlsxFieldRepository repository;

    public Map<SkorozvonField, AutoLinkXlsxFieldDto> getAll() {
        Map<SkorozvonField, List<AutoLinkXlsxFieldEntity>> map = repository.findAll().stream()
                .collect(groupingBy(AutoLinkXlsxFieldEntity::getField));
        SkorozvonField.selectValues()
                .forEach(field -> map.putIfAbsent(field, Collections.emptyList()));
        return map.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> {
                    AutoLinkXlsxFieldDto dto = new AutoLinkXlsxFieldDto();
                    dto.setField(entry.getKey());
                    dto.setColumns(entry.getValue().stream()
                            .filter(column -> Objects.nonNull(column.getColumn()))
                            .map(AutoLinkXlsxFieldEntity::getColumn).collect(Collectors.toSet()));
                    return dto;
                }));
    }

    public AutoLinkXlsxFieldDto getByField(SkorozvonField field) {
        AutoLinkXlsxFieldDto dto = new AutoLinkXlsxFieldDto();
        List<AutoLinkXlsxFieldEntity> entities = repository.findAllByField(field);
        dto.setColumns(entities.stream()
                .filter(name -> Objects.nonNull(name.getColumn()))
                .map(AutoLinkXlsxFieldEntity::getColumn)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Transactional
    public void create(List<AutoLinkXlsxFieldDto> input) {
        repository.deleteAll();
        List<AutoLinkXlsxFieldEntity> entities = input.stream()
                .flatMap(field -> {
                    List<AutoLinkXlsxFieldEntity> fnvList = field.getColumns().stream()
                            .map(columnNumber -> {
                                AutoLinkXlsxFieldEntity entity = new AutoLinkXlsxFieldEntity();
                                entity.setField(field.getField());
                                entity.setColumn(columnNumber);
                                return entity;
                            }).collect(Collectors.toList());
                    return fnvList.stream();
                }).collect(Collectors.toList());
        repository.saveAll(entities);
    }
}
