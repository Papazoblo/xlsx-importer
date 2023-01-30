package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.ProjectNumberDto;
import ru.medvedev.importer.entity.ProjectNumberEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.ProjectNumberRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectNumberService {

    private final ProjectNumberRepository repository;

    public Page<ProjectNumberDto> getPage(Pageable pageable) {
        Page<ProjectNumberEntity> page = repository.findAll(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(ProjectNumberDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public Long getNumberByDate(Bank bank, LocalDate date) {
        return repository.findByBankAndDate(bank, date).map(item -> Long.valueOf(item.getNumber()))
                .orElse(null);
    }

    public void create(ProjectNumberDto input) {
        ProjectNumberEntity entity = new ProjectNumberEntity();
        entity.setNumber(input.getNumber());
        entity.setDate(LocalDate.parse(input.getDate(), DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        entity.setBank(input.getBank());

        if (repository.existsByDateAndBank(entity.getDate(), entity.getBank())) {
            throw new BadRequestException("Проект для указанной даты и банка уже установлен");
        }
        repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
