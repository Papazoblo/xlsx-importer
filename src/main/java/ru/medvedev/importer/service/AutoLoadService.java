package ru.medvedev.importer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.AutoLoadDto;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.entity.AutoLoadEntity;
import ru.medvedev.importer.repository.AutoLoadRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AutoLoadService {

    private final AutoLoadRepository repository;
    private final ObjectMapper objectMapper;

    public List<AutoLoadDto> findEnabledLoad() {
        return repository.findAllByEnabledIsTrue().stream()
                .map(this::toDto).collect(toList());
    }

    public List<AutoLoadDto> getAll() {
        return repository.findAllOrderByIdDesc().stream()
                .map(this::toDto).collect(toList());
    }

    public void create(AutoLoadDto dto) {
        AutoLoadEntity entity = new AutoLoadEntity();
        try {
            entity.setFilter(objectMapper.writeValueAsString(dto.getFilter()));
        } catch (JsonProcessingException e) {
            entity.setFilter("");
        }
        entity.setInterval(dto.getInterval());
        entity.setPeriod(dto.getPeriod());
        entity.setProjectId(dto.getProjectId());
        repository.save(entity);
    }

    public void update(Long id, AutoLoadDto dto) {
        AutoLoadEntity entity = getById(id);
        try {
            entity.setFilter(objectMapper.writeValueAsString(dto.getFilter()));
        } catch (JsonProcessingException e) {
            entity.setFilter("");
        }
        entity.setProjectId(dto.getProjectId());
        entity.setPeriod(dto.getPeriod());
        entity.setInterval(dto.getInterval());
        repository.save(entity);
    }

    public void enable(Long id) {
        AutoLoadEntity entity = getById(id);
        entity.setEnabled(true);
        repository.save(entity);
    }

    public void disable(Long id) {
        AutoLoadEntity entity = getById(id);
        entity.setEnabled(false);
        repository.save(entity);
    }

    public void delete(Long id) {
        AutoLoadEntity entity = getById(id);
        entity.setDeleted(true);
        repository.save(entity);
    }

    private AutoLoadEntity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    private AutoLoadDto toDto(AutoLoadEntity entity) {
        AutoLoadDto dto = new AutoLoadDto();
        dto.setId(entity.getId());
        dto.setCreateDate(entity.getCreateDate());
        try {
            dto.setFilter(objectMapper.readValue(entity.getFilter(), ContactFilter.class));
        } catch (JsonProcessingException ex) {
            dto.setFilter(new ContactFilter());
        }
        dto.setEnabled(entity.getEnabled());
        dto.setInterval(entity.getInterval());
        dto.setLastLoad(entity.getLastLoad());
        dto.setPeriod(entity.getPeriod());
        dto.setProjectId(entity.getProjectId());
        return dto;
    }
}
