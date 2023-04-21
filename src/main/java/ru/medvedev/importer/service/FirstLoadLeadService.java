package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.FirstLoadLeadEntity;
import ru.medvedev.importer.repository.FirstLoadLeadRepository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FirstLoadLeadService {

    private final FirstLoadLeadRepository repository;

    @Scheduled(cron = "${cron.first-load-lead-delete}")
    public void deleteByDate() {
        List<FirstLoadLeadEntity> entities = repository.findAllByCreateDateLessThan(LocalDateTime.now().minusDays(90));
        repository.deleteAll(entities);
    }

    public void create(String inn) {
        if (!existsByInn(inn)) {
            FirstLoadLeadEntity entity = new FirstLoadLeadEntity();
            entity.setInn(inn);
            repository.save(entity);
        }
    }

    public boolean existsByInn(String inn) {
        return repository.existsByInn(inn);
    }
}
