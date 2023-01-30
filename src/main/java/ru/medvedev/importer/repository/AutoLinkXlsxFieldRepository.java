package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.AutoLinkXlsxFieldEntity;
import ru.medvedev.importer.enums.SkorozvonField;

import java.util.List;

@Repository
public interface AutoLinkXlsxFieldRepository extends JpaRepository<AutoLinkXlsxFieldEntity, Long> {

    List<AutoLinkXlsxFieldEntity> findAllByField(SkorozvonField field);

}
