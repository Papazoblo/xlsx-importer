package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import ru.medvedev.importer.dto.FileInfoDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.dto.events.CompleteFileEvent;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.InvalidFileEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.FileInfoRepository;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static ru.medvedev.importer.enums.FileProcessingStep.DOWNLOADED;
import static ru.medvedev.importer.enums.FileProcessingStep.INITIALIZE;
import static ru.medvedev.importer.enums.FileSource.TELEGRAM;
import static ru.medvedev.importer.enums.FileSource.UI;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileInfoService {

    private final FileInfoRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public Page<FileInfoDto> getPage(Pageable pageable) {
        Page<FileInfoEntity> page = repository.findAllByDeletedIsFalse(pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(FileInfoDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public void save(FileInfoEntity entity) {
        repository.save(entity);
    }

    public Long getChatIdByFile(Long fileId) {
        return repository.findById(fileId).map(FileInfoEntity::getChatId).orElse(null);
    }

    public Optional<FileInfoEntity> getDownloadedFile() {
        return repository.findFirstByStatusAndProcessingStepOrderByCreateAt(FileStatus.DOWNLOADED, INITIALIZE);
    }

    public Optional<FileInfoEntity> getFileInProcess() {
        return repository.findByStatusAndSource(FileStatus.IN_PROCESS, TELEGRAM);
    }

    public Optional<FileInfoEntity> getDownloadedUiFile() {
        return repository.findFirstByProcessingStepAndSourceAndStatus(DOWNLOADED, UI, FileStatus.DOWNLOADED);
    }

    public Optional<FileInfoEntity> getFileToTgRequest() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Arrays.asList(FileProcessingStep.RESPONSE_COLUMN_NAME, FileProcessingStep.RESPONSE_REQUIRE_FIELD));
    }

    public Optional<FileInfoEntity> getFileWaitColumnResponse() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(FileProcessingStep.REQUEST_COLUMN_NAME));
    }

    public Optional<FileInfoEntity> getFileWaitRequireColumnResponse() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(FileProcessingStep.REQUEST_REQUIRE_FIELD));
    }

    public Optional<FileInfoEntity> getFileToProcessingBody() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(FileProcessingStep.WAIT_READ_DATA));
    }

    public Map<Long, String> getFileNameMapByIds(Set<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(toMap(FileInfoEntity::getId, FileInfoEntity::getName));
    }

    public boolean create(Document document, Long chatId, File file, FileSource source) {

        String hash = hashFile(file.toPath());
        if (!repository.existsByHashAndStatus(hash, FileStatus.SUCCESS)) {
            FileInfoEntity entity = new FileInfoEntity();
            entity.setName(document.getFileName());
            entity.setSize(Long.valueOf(document.getFileSize()));
            entity.setType(document.getMimeType());
            entity.setUniqueId(document.getFileUniqueId());
            entity.setTgFileId(document.getFileId());
            entity.setHash(hash);
            entity.setPath(file.getPath());
            entity.setDeleted(false);
            entity.setChatId(chatId);
            entity.setSource(source);
            entity.setProcessingStep(INITIALIZE);
            entity = repository.save(entity);
            eventPublisher.publishEvent(new ImportEvent(this, "Добавлен в систему и ждет своей очереди",
                    EventType.LOG_TG, entity.getId()));
            log.debug("*** create file with hash {}", hash);
            return true;
        } else {
            eventPublisher.publishEvent(new ImportEvent(this, "Файл *" + file.getName() + "* ранее был успешно обработан системой",
                    EventType.LOG_TG, -1L));
            log.debug("*** file with hash {} already exist", hash);
            return false;
        }
    }

    public boolean create(MultipartFile multipartFile, Long chatId, File file, FileSource source) {

        String hash = hashFile(file.toPath());
        if (!repository.existsByHashAndStatus(hash, FileStatus.SUCCESS)) {
            FileInfoEntity entity = new FileInfoEntity();
            entity.setName(multipartFile.getOriginalFilename());
            entity.setSize(multipartFile.getSize());
            entity.setType(multipartFile.getContentType());
            entity.setHash(hash);
            entity.setPath(file.getPath());
            entity.setDeleted(false);
            entity.setChatId(chatId);
            entity.setSource(source);
            entity.setUniqueId("");
            entity.setTgFileId("");
            entity.setProcessingStep(DOWNLOADED);
            repository.save(entity);
            eventPublisher.publishEvent(new ImportEvent(this, "Добавлен в систему через интерфейс и ждет указания столбцов для дальнейшей обратки",
                    EventType.LOG_TG, entity.getId()));
            log.debug("*** create file with hash {}", hash);
            return true;
        } else {
            eventPublisher.publishEvent(new ImportEvent(this, "Файл *" + file.getName() + "* ранее был успешно обработан системой",
                    EventType.LOG_TG, -1L));
            log.debug("*** file with hash {} already exist", hash);
            return false;
        }
    }

    public void addUiColumnInfo(XlsxImportInfo xlsxImportInfo) {
        repository.findById(xlsxImportInfo.getFileId()).map(file -> {
            file.setProcessingStep(INITIALIZE);
            file.setProjectId(xlsxImportInfo.getProjectCode().toString());
            file.setEnableWhatsAppLink(xlsxImportInfo.isEnableWhatsAppLink());
            file.setFieldLinks(xlsxImportInfo.getFieldLinks());
            file.setOrgTags(xlsxImportInfo.getOrgTags());
            repository.save(file);
            eventPublisher.publishEvent(new ImportEvent(this, "Добавлена информация о колонках. Файл ожидает обработки", EventType.LOG_TG,
                    file.getId()));
            return true;
        }).orElseThrow(() -> {
            log.debug("Файл с id {} не найден", xlsxImportInfo.getFileId());
            return new BadRequestException(String.format("Файл с id %d не найден", xlsxImportInfo.getFileId()));
        });
    }

    public void delete(Long id) {
        FileInfoEntity entity = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        deleteFile(entity.getId());
        if (entity.getStatus() == FileStatus.DOWNLOADED) {
            repository.delete(entity);
        } else {
            entity.setDeleted(true);
            repository.save(entity);
        }
    }

    public FileInfoEntity getById(Long id) {
        return repository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public boolean isExistsInProcess() {
        return repository.existsByStatus(FileStatus.IN_PROCESS);
    }

    public FileInfoEntity changeStatus(FileInfoEntity entity, FileStatus status) {
        repository.changeStatus(entity.getId(), status);
        entity.setStatus(status);
        return entity;
    }

    @EventListener(InvalidFileEvent.class)
    public void invalidFileEventListener(InvalidFileEvent event) {
        repository.changeStatus(event.getFileId(), FileStatus.ERROR);
        deleteFile(event.getFileId());
    }

    @EventListener(CompleteFileEvent.class)
    public void completeFileEventListener(CompleteFileEvent event) {
        repository.changeStatus(event.getFileId(), FileStatus.SUCCESS);
        deleteFile(event.getFileId());
    }

    private void deleteFile(Long fileId) {
        repository.findById(fileId).ifPresent(file -> {
            new File(file.getPath()).delete();
        });
    }

    @SneakyThrows
    public static String hashFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return md5Hex(is);
        }
    }
}
