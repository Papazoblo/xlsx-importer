package ru.medvedev.importer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.request.DownloadFilterRequest;
import ru.medvedev.importer.dto.response.DownloadFilterDto;
import ru.medvedev.importer.entity.DownloadFilterEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.DownloadFilter;
import ru.medvedev.importer.repository.DownloadFilterRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadFilterService {

    private final DownloadFilterRepository repository;

    public List<DownloadFilterDto> getByCode(DownloadFilter code) {
        return repository.findAllByCode(code).stream()
                .map(entity -> {
                    DownloadFilterDto dto = new DownloadFilterDto();
                    dto.setId(entity.getId());
                    dto.setCode(entity.getCode());
                    dto.setValue(entity.getValue());
                    dto.setBank(entity.getBank());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<DownloadFilterEntity> getByCodeAndBank(DownloadFilter code, Bank bank) {
        return repository.findAllByCodeAndBank(code, bank);
    }

    public void saveFilter(DownloadFilter code, DownloadFilterRequest request) {
        repository.deleteByCode(code);

        repository.saveAll(request.getFilter().stream()
                .map(item -> {
                    DownloadFilterEntity entity = new DownloadFilterEntity();
                    entity.setCode(code);
                    entity.setBank(item.getBank());
                    entity.setValue(item.getValue());
                    return entity;
                }).collect(Collectors.toList()));
    }
}
