package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.CityDto;
import ru.medvedev.importer.entity.CityEntity;
import ru.medvedev.importer.enums.Bank;
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

    public void refreshList(Bank bank, List<CityDto> cities) {
        Map<String, CityEntity> mapByName = repository.findAll().stream()
                .collect(toMap(item -> item.getName().toLowerCase().trim(), item -> item));

        repository.saveAll(cities.stream()
                .map(newCity -> Optional.ofNullable(mapByName.get(newCity.getName().toLowerCase().trim()))
                        .map(oldCity -> {
                            if (bank == Bank.RAIFFEISEN) {
                                oldCity.setRaiffeisenId(newCity.getRaiffeisenId());
                            } else if (bank == Bank.ALFA) {
                                oldCity.setAlfaId(newCity.getAlfaId());
                            }
                            return oldCity;
                        })
                        .orElseGet(() -> {
                            CityEntity city = new CityEntity();
                            city.setName(newCity.getName());
                            if (bank == Bank.RAIFFEISEN) {
                                city.setRaiffeisenId(newCity.getRaiffeisenId());
                            } else if (bank == Bank.ALFA) {
                                city.setAlfaId(newCity.getAlfaId());
                            }
                            mapByName.put(city.getName().toLowerCase().trim(), city);
                            return city;
                        }))
                .collect(Collectors.toList()));
    }
}
