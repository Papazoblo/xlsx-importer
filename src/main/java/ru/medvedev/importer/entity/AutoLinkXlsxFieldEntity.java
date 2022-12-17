package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.SkorozvonField;

import javax.persistence.*;

@Entity
@Table(name = "auto_link_xlsx_field")
@Data
public class AutoLinkXlsxFieldEntity {

    @Id
    @SequenceGenerator(name = "auto_link_xlsx_field_seq_gen", sequenceName = "auto_link_xlsx_field_seq_id", allocationSize = 1)
    @GeneratedValue(generator = "auto_link_xlsx_field_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "field")
    @Enumerated(EnumType.STRING)
    private SkorozvonField field;

    @Column(name = "column_number")
    private Integer column;
}
