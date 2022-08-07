package ru.medvedev.importer.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import ru.medvedev.importer.dto.ColumnInfoDto;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.FileStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.medvedev.importer.enums.FileProcessingStep.INITIALIZE;

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

    @Column(name = "source")
    @Enumerated(EnumType.STRING)
    private FileSource source;

    @Column(name = "processing_step")
    @Enumerated(EnumType.STRING)
    private FileProcessingStep processingStep;

    @Column(name = "column_info")
    private String columnInfo;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "with_header")
    private Boolean withHeader;

    @Column(name = "ask_column_number")
    private Integer askColumnNumber;

    @OneToMany(mappedBy = "fileInfoEntity", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<FileRequestEmptyRequireFieldEntity> fileRequestEmptyRequireFieldEntities = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
        status = FileStatus.DOWNLOADED;
        processingStep = INITIALIZE;
    }

    public void setColumnInfo(ColumnInfoDto columnInfo) {
        try {
            this.columnInfo = new ObjectMapper().writeValueAsString(columnInfo);
        } catch (JsonProcessingException e) {
            this.columnInfo = "";
        }
    }

    public Optional<ColumnInfoDto> getColumnInfo() {
        try {
            return Optional.of(new ObjectMapper().readValue(this.columnInfo, ColumnInfoDto.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}

