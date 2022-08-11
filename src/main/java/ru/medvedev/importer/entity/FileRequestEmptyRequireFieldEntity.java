package ru.medvedev.importer.entity;

import lombok.Data;
import lombok.ToString;
import ru.medvedev.importer.enums.XlsxRequireField;

import javax.persistence.*;

@Entity
@Table(name = "file_request_empty_require_column")
@Data
public class FileRequestEmptyRequireFieldEntity {

    @Id
    @SequenceGenerator(name = "file_request_empty_require_column_seq", allocationSize = 1, sequenceName = "file_request_empty_require_column_seq")
    @GeneratedValue(generator = "file_request_empty_require_column_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileInfoEntity fileInfoEntity;

    @Column(name = "`column`")
    private XlsxRequireField column;

    @Column(name = "have_answer")
    private Boolean haveAnswer;

    @PrePersist
    public void prePersist() {
        haveAnswer = false;
    }

}