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
import ru.medvedev.importer.dto.events.SaveMessageIdEvent;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.*;
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
import static ru.medvedev.importer.enums.FileProcessingStep.*;
import static ru.medvedev.importer.enums.FileSource.TELEGRAM;
import static ru.medvedev.importer.enums.FileSource.UI;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileInfoService {

    private final FileInfoRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestService requestService;

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

    public List<FileInfoBankEntity> getLastTgFileProjectCode(Bank bank) {
        if (bank == null) {
            return Collections.emptyList();
        }
        List<FileInfoBankEntity> projects = repository.getLastUiProjectCode(bank, TELEGRAM,
                Arrays.asList(FileStatus.SUCCESS, FileStatus.IN_PROCESS));
        if (!projects.isEmpty()) {
            return projects.stream()
                    .filter(fib -> !fib.getFileInfoId().equals(projects.get(0).getFileInfoId()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Optional<FileInfoEntity> getNewUiFileToInitialize() {
        return repository.findFirstByProcessingStepAndSourceAndStatus(IN_QUEUE, UI, FileStatus.NEW);
    }

    public Optional<FileInfoEntity> getTgFileToSelectBank() {
        return repository.findFirstByProcessingStepAndSourceAndStatus(INITIALIZE, TELEGRAM, FileStatus.IN_PROCESS);
    }

    public Optional<FileInfoEntity> getTgFileToSetProjectCode() {
        return repository.findFirstByProcessingStepAndSourceAndStatus(BANK_INITIALIZED, TELEGRAM, FileStatus.IN_PROCESS);
    }

    public Optional<FileInfoEntity> getNewFileToProcessing() {
        return repository.findFirstByStatusAndProcessingStepOrderByCreateAt(FileStatus.NEW, INITIALIZE);
    }

    public List<FileInfoEntity> getWaitingFile() {
        return repository.findAllByStatus(FileStatus.WAITING_CHECK);
    }

    public Optional<FileInfoEntity> getFileInProcess() {
        return repository.findByStatusAndSource(FileStatus.IN_PROCESS, TELEGRAM);
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

    public Optional<FileInfoEntity> getFileWaitProjectCode() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(WAIT_PROJECT_CODE_INITIALIZE));
    }

    public Optional<FileInfoEntity> getFileWaitBankInitialize() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(WAIT_BANK_INITIALIZE));
    }

    public Optional<FileInfoEntity> getFileToProcessingBody() {
        return repository.findByStatusAndSourceAndProcessingStepIn(FileStatus.IN_PROCESS, TELEGRAM,
                Collections.singletonList(FileProcessingStep.WAIT_READ_DATA));
    }

    public Map<Long, String> getFileNameMapByIds(Set<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(toMap(FileInfoEntity::getId, FileInfoEntity::getName));
    }

    //телеграм
    public FileInfoEntity create(Document document, Long chatId, File file, FileSource source) {

        String hash = hashFile(file.toPath());
        if (repository.existsByHashAndStatusNot(hash, FileStatus.ERROR)) {
            eventPublisher.publishEvent(new ImportEvent(this, "Файл `" + file.getName() + "` ранее был успешно обработан системой",
                    EventType.LOG_TG, -1L));
            log.debug("*** file with hash {} already exist", hash);
            return null;
        } else {
            FileInfoEntity entity = new FileInfoEntity();
            entity.setName(document.getFileName());
            entity.setSize(Long.valueOf(document.getFileSize()));
            entity.setType(document.getMimeType());
            entity.setUniqueId(document.getFileUniqueId());
            entity.setTgFileId(document.getFileId());
            entity.setHash(hash);
            entity.setPath(file.getPath());
            entity.setChatId(chatId);
            entity.setSource(source);
            entity.setProcessingStep(INITIALIZE);
            entity = repository.save(entity);
            eventPublisher.publishEvent(new ImportEvent(this, "Добавлен в систему и ждет своей очереди",
                    EventType.LOG_TG, entity.getId()));
            log.debug("*** create file with hash {}", hash);
            return entity;
        }
    }

    //интерфейс
    public FileInfoEntity create(MultipartFile multipartFile, Long chatId, File file, FileSource source) {

        String hash = hashFile(file.toPath());
        if (repository.existsByHashAndStatusNot(hash, FileStatus.ERROR)) {
            eventPublisher.publishEvent(new ImportEvent(this, "Файл `" + file.getName() + "` ранее был успешно обработан системой",
                    EventType.LOG_TG, -1L));
            log.debug("*** file with hash {} already exist", hash);
            return null;
        } else {
            FileInfoEntity entity = new FileInfoEntity();
            entity.setName(multipartFile.getOriginalFilename());
            entity.setSize(multipartFile.getSize());
            entity.setType(multipartFile.getContentType());
            entity.setHash(hash);
            entity.setPath(file.getPath());
            entity.setChatId(chatId);
            entity.setSource(source);
            entity.setUniqueId("");
            entity.setTgFileId("");
            entity.setProcessingStep(IN_QUEUE);
            entity = repository.save(entity);
            eventPublisher.publishEvent(new ImportEvent(this, source == UI ? "Добавлен в систему и ждет указания столбцов для дальнейшей обратки"
                    : "Добавлен в систему",
                    EventType.LOG_TG, entity.getId()));
            log.debug("*** create file with hash {}", hash);
            return entity;
        }
    }

    //добавляем информацию о столбцах
    public void addUiColumnInfo(XlsxImportInfo xlsxImportInfo) {
        repository.findById(xlsxImportInfo.getFileId()).map(file -> {
            file.setProcessingStep(INITIALIZE);
            file.getBankList().addAll(xlsxImportInfo.getBanksProject().entrySet().stream()
                    .map(bankEntry -> {
                        FileInfoBankEntity bankEntity = new FileInfoBankEntity();
                        bankEntity.setBank(bankEntry.getKey());
                        bankEntity.setFileInfo(file);
                        bankEntity.setProjectId(bankEntry.getValue());
                        return bankEntity;
                    }).collect(Collectors.toList()));
            file.setEnableWhatsAppLink(xlsxImportInfo.isEnableWhatsAppLink());
            file.setFieldLinks(xlsxImportInfo.getFieldLinks());
            file.setOrgTags(xlsxImportInfo.getOrgTags());
            repository.save(file);
            eventPublisher.publishEvent(new ImportEvent(this, "Добавлена информация о столбцах. " +
                    "Файл ожидает обработки", EventType.LOG_TG, file.getId()));
            return true;
        }).orElseThrow(() -> {
            log.debug("Файл с id {} не найден", xlsxImportInfo.getFileId());
            eventPublisher.publishEvent(new ImportEvent(this,
                    String.format("Файл с id %d не найден", xlsxImportInfo.getFileId()), EventType.LOG_TG,
                    xlsxImportInfo.getFileId()));
            return new BadRequestException(String.format("Файл с id %d не найден", xlsxImportInfo.getFileId()));
        });
    }

    public void delete(Long id) {
        FileInfoEntity entity = repository.findById(id).orElseThrow(EntityNotFoundException::new);
        deleteFile(entity.getId());

        Set<Long> fibIds = entity.getBankList().stream().map(FileInfoBankEntity::getId)
                .collect(Collectors.toSet());
        if(!fibIds.isEmpty()) {
            requestService.deleteByFibId(fibIds);
        }
        entity.getBankList().clear();

        if (entity.getStatus() == FileStatus.NEW) {
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

    @EventListener(SaveMessageIdEvent.class)
    public void saveMessageIdEventListener(SaveMessageIdEvent event) {
        repository.updateMessageId(event.getFileId(), event.getMessageId());
    }

    private void deleteFile(Long fileId) {
        repository.findById(fileId).ifPresent(file ->
                new File(file.getPath()).delete());
    }

    @SneakyThrows
    public static String hashFile(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return md5Hex(is);
        }
    }
}
