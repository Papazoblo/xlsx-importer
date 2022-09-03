package ru.medvedev.importer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.request.DownloadFilterRequest;
import ru.medvedev.importer.dto.response.DownloadFilterDto;
import ru.medvedev.importer.entity.DownloadFilterEntity;
import ru.medvedev.importer.enums.DownloadFilter;
import ru.medvedev.importer.repository.DownloadFilterRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadFilterService {

    private final DownloadFilterRepository repository;
    private final ObjectMapper objectMapper;

    public DownloadFilterDto getByName(DownloadFilter filterName) {
        return repository.findByName(filterName)
                .map(entity -> {
                    DownloadFilterDto dto = new DownloadFilterDto();
                    dto.setId(entity.getId());
                    dto.setName(entity.getName());
                    try {
                        dto.setFilter(objectMapper.readValue(entity.getFilter(), entity.getName().getType()));
                    } catch (JsonProcessingException e) {
                        log.debug("*** Error convert string to DownloadFilter");
                        dto.setFilter(entity.getName().getDefaultValue());
                    }
                    return dto;
                })
                .orElseGet(() -> {
                    DownloadFilterRequest request = new DownloadFilterRequest();
                    request.setName(DownloadFilter.INN);
                    request.setFilter("[]");
                    saveFilter(request);
                    return getByName(filterName);
                });
    }

    public void saveFilter(DownloadFilterRequest request) {
        repository.deleteByName(request.getName());

        DownloadFilterEntity entity = new DownloadFilterEntity();
        entity.setName(request.getName());
        entity.setFilter(String.valueOf(request.getFilter()));
        repository.save(entity);
    }
}
