package ru.medvedev.importer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.medvedev.importer.entity.FieldNameVariantEntity;
import ru.medvedev.importer.enums.XlsxRequireField;

import java.util.List;

@Repository
public interface FieldNameVariantRepository extends JpaRepository<FieldNameVariantEntity, Long> {

    List<FieldNameVariantEntity> findAllByField(XlsxRequireField field);
}
