package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.CityDto;
import ru.medvedev.importer.entity.CityEntity;
import ru.medvedev.importer.repository.CityRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@RequiredArgsConstructor
@Service
public class CityService {

    private final CityRepository repository;

    public Optional<CityEntity> getByName(String name) {
        return repository.findByName(name);
    }

    public void refreshList(List<CityDto> cities) {
        Map<String, CityEntity> mapByName = repository.findAll().stream()
                .collect(toMap(CityEntity::getName, item -> item));

        repository.saveAll(cities.stream()
                .map(newCity -> Optional.ofNullable(mapByName.get(newCity.getName()))
                        .map(oldCity -> {
                            oldCity.setRaiffeisenId(newCity.getRaiffeisenId());
                            return oldCity;
                        })
                        .orElseGet(() -> {
                            CityEntity city = new CityEntity();
                            city.setName(newCity.getName());
                            city.setRaiffeisenId(newCity.getRaiffeisenId());
                            return city;
                        }))
                .collect(Collectors.toList()));
    }
}
