package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.SystemVariableEntity;
import ru.medvedev.importer.enums.SystemVariable;

@Repository
public interface SystemVariableRepository extends JpaRepository<SystemVariableEntity, SystemVariable> {
}
