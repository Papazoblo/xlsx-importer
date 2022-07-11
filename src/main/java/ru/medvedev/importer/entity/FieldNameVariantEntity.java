package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.XlsxRequireField;

import javax.persistence.*;

@Entity
@Table(name = "field_name_variants")
@Data
public class FieldNameVariantEntity {

    @Id
    @SequenceGenerator(name = "field_name_variants_seq_gen", sequenceName = "field_name_variants_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "field_name_variants_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "field")
    @Enumerated(EnumType.STRING)
    private XlsxRequireField field;

    @Column(name = "name")
    private String name;

    @Column(name = "required")
    private boolean required;
}
