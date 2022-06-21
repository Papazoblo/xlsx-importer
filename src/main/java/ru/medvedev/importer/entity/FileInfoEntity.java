package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.FileStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_info")
@Data
public class FileInfoEntity {

    @Id
    @SequenceGenerator(name = "file_info_generator_id", allocationSize = 1, sequenceName = "file_info_seq_id")
    @GeneratedValue(generator = "file_info_generator_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "size")
    private Long size;

    @Column(name = "type")
    private String type;

    @Column(name = "unique_id")
    private String uniqueId;

    @Column(name = "tg_file_id")
    private String tgFileId;

    @Column(name = "hash")
    private String hash;

    @Column(name = "path")
    private String path;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "deleted")
    private boolean deleted;

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
        status = FileStatus.DOWNLOADED;
    }
}

