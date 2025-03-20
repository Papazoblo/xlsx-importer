package ru.medvedev.importer.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import static org.hibernate.internal.util.StringHelper.isBlank;
import ru.medvedev.importer.dto.ColumnInfoDto;
import ru.medvedev.importer.enums.DownloadAction;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.enums.SkorozvonField;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    @Column(name = "enable_whats_app_link")
    private Boolean enableWhatsAppLink = false;
    @Column(name = "is_first_vtb_load_lead")
    private Boolean isFirstVtbLoadLead = false;
    @Column(name = "is_not_load_from_vtb_dedublicate")
    private Boolean notLoadFromVtbDedublicate = false;
    @Column(name = "is_enable_inn_exclude")
    private Boolean enableInnExclude = false;
    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private DownloadAction action;
    //енам скорозвона => список идентификаторов колонок экселя
    @Column(name = "field_links")
    private String fieldLinks;

    @Column(name = "org_tags")
    private String orgTags;

    @Column(name = "message_id")
    private Integer messageId;

    @OneToMany(mappedBy = "fileInfoEntity", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<FileRequestEmptyRequireFieldEntity> fileRequestEmptyRequireFieldEntities = new HashSet<>();

    @OneToMany(mappedBy = "fileInfo", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<FileInfoBankEntity> bankList = new ArrayList<>();

    @OneToMany(mappedBy = "fileInfo", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CheckLeadInfoEntity> leadToCheckList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
        status = FileStatus.NEW;
        deleted = false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size, type, uniqueId, tgFileId, hash,
                path, status, source, processingStep, columnInfo);
    }

    public Boolean getWithHeader() {
        return withHeader != null || withHeader;
    }

    public void setOrgTags(List<String> tags) {
        try {
            this.orgTags = new ObjectMapper().writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            this.orgTags = "";
        }
    }

    public void setFieldLinks(Map<SkorozvonField, List<Integer>> fieldLinks) {
        try {
            this.fieldLinks = new ObjectMapper().writeValueAsString(fieldLinks);
        } catch (JsonProcessingException e) {
            this.fieldLinks = "";
        }
    }

    public void setColumnInfo(ColumnInfoDto columnInfo) {
        try {
            this.columnInfo = new ObjectMapper().writeValueAsString(columnInfo);
        } catch (JsonProcessingException e) {
            this.columnInfo = "";
        }
    }

    public List<String> getOrgTags() {
        try {
            return new ObjectMapper().readValue(this.orgTags, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public Map<SkorozvonField, List<Integer>> getFieldLinks() {
        try {
            return new ObjectMapper().readValue(this.fieldLinks, new TypeReference<Map<SkorozvonField, List<Integer>>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    public Optional<ColumnInfoDto> getColumnInfo() {
        try {
            if (isBlank(this.columnInfo)) {
                this.columnInfo = "";
            }
            return Optional.of(new ObjectMapper().readValue(this.columnInfo, ColumnInfoDto.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}

