package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.EnabledScenarioEntity;
import ru.medvedev.importer.entity.FirstLoadLeadEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FirstLoadLeadRepository extends JpaRepository<FirstLoadLeadEntity, String> {

    boolean existsByInn(String inn);

    List<FirstLoadLeadEntity> findAllByCreateDateLessThan(LocalDateTime date);
}
