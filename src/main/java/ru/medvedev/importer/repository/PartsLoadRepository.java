package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.PartsLoadEntity;

import java.util.List;

@Repository
public interface PartsLoadRepository extends JpaRepository<PartsLoadEntity, Long> {

    @Query("select p from PartsLoadEntity p " +
            "where p.status <> ru.medvedev.importer.enums.PartsLoadStatus.FINISHED")
    List<PartsLoadEntity> findAllByStatusNotFinished();
}
