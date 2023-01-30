package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.AutoLoadEntity;

import java.util.List;

@Repository
public interface AutoLoadRepository extends JpaRepository<AutoLoadEntity, Long> {

    @Query("select al from AutoLoadEntity al where al.deleted = false order by al.id desc")
    List<AutoLoadEntity> findAllOrderByIdDesc();

    List<AutoLoadEntity> findAllByEnabledIsTrue();
}
