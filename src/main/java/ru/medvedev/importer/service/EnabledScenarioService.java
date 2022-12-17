package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.EnabledScenarioDto;
import ru.medvedev.importer.entity.EnabledScenarioEntity;
import ru.medvedev.importer.repository.EnabledScenarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnabledScenarioService {

    private final EnabledScenarioRepository repository;

    public List<EnabledScenarioDto> getAll() {
        return repository.findAll().stream()
                .map(entity -> {
                    EnabledScenarioDto dto = new EnabledScenarioDto();
                    dto.setId(entity.getId());
                    dto.setScenarioId(entity.getScenarioId());
                    dto.setBank(entity.getBank());
                    return dto;
                }).collect(Collectors.toList());
    }

    public Optional<EnabledScenarioEntity> findByScenarioId(Long scenarioId) {
        return repository.findByScenarioId(scenarioId);
    }

    public boolean existsByScenarioId(Long scenarioId) {
        return repository.existsByScenarioId(scenarioId);
    }

    public void create(EnabledScenarioDto dto) {
        if (dto.getScenarioId() != null && dto.getBank() != null && !existsByScenarioId(dto.getId())) {
            EnabledScenarioEntity entity = new EnabledScenarioEntity();
            entity.setScenarioId(dto.getScenarioId());
            entity.setBank(dto.getBank());
            repository.save(entity);
        }
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
