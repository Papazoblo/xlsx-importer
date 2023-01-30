package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.dto.ColumnInfoDto;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileSource;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.exception.ColumnNamesNotFoundException;
import ru.medvedev.importer.exception.FileProcessingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final FileInfoService fileInfoService;
    private final FieldNameVariantService fieldNameVariantService;
    private final ApplicationEventPublisher eventPublisher;
    private final HeaderProcessingService headerProcessingService;
    private final LeadWorkerService leadWorkerService;
    private final ContactNewService contactNewService;
    private final XlsxStorageService xlsxStorageService;


    @Scheduled(cron = "${cron.launch-file-processing}")
    public void launchProcessFile() {
        if (fileInfoService.isExistsInProcess()) {
            return;
        }

        fileInfoService.getNewFileToProcessing().ifPresent(entity -> {
            log.debug("*** launch file processing [{}, id = {}]", entity.getName(), entity.getId());
            eventPublisher.publishEvent(new ImportEvent(this, "Взят в обработку",
                    EventType.FILE_PROCESS, entity.getId(), true));
            entity = fileInfoService.changeStatus(entity, FileStatus.IN_PROCESS);
            try {
                if (entity.getSource() == FileSource.UI) {
                    leadWorkerService.processXlsxRecords(entity);
                    fileInfoService.changeStatus(entity, FileStatus.WAITING);
                } else {
                    launchProcessTelegramFile(entity);
                }
            } catch (FileProcessingException ex) {
                log.debug("Error processing file: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.ERROR,
                        ex.getFileId()));
            } catch (Exception ex) {
                log.debug("Error processing file", ex);
                eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                        .orElse("Непредвиденная ошибка"), EventType.ERROR,
                        entity.getId()));
            }
        });
    }

    public void importContacts(MultipartFile file, XlsxImportInfo info) throws IOException {

        FileInfoEntity fileInfoEntity = xlsxStorageService.upload(file);
        if (fileInfoEntity == null) {
            return;
        }
        Set<String> innList = contactNewService.importContacts(file);

        fileInfoEntity.getBankList().addAll(info.getBanksProject().entrySet().stream()
                .map(bankEntry -> {
                    FileInfoBankEntity bankEntity = new FileInfoBankEntity();
                    bankEntity.setBank(bankEntry.getKey());
                    bankEntity.setFileInfo(fileInfoEntity);
                    bankEntity.setProjectId(bankEntry.getValue());
                    return bankEntity;
                }).collect(Collectors.toList()));

        if (innList.isEmpty()) {
            //todo уведомление о пустом списке для импорта
        } else {
            leadWorkerService.processXlsxRecords(fileInfoEntity, innList);
        }
    }

    private void launchProcessTelegramFile(FileInfoEntity entity) {
        Map<XlsxRequireField, FieldNameVariantDto> namesMap = fieldNameVariantService.getAll();
        if (namesMap.keySet().stream().filter(field -> field != XlsxRequireField.TRASH)
                .anyMatch(key -> namesMap.get(key).getNames().isEmpty() && namesMap.get(key).isRequired())) {
            throw new ColumnNamesNotFoundException("Не указаны варианты названий полей", entity.getId());
        } else {
            readFile(entity, namesMap);
        }
    }

    private void readFile(FileInfoEntity entity, Map<XlsxRequireField, FieldNameVariantDto> namesMap) {
        try {
            FileInputStream fis = new FileInputStream(new File(entity.getPath()));
            ColumnInfoDto columnInfoDto = headerProcessingService.headerProcessing(entity, namesMap, fis);
            entity.setColumnInfo(columnInfoDto);
            fileInfoService.save(entity);
            fis.close();
        } catch (IOException e) {
            throw new FileProcessingException("Невозможно открыть файл", entity.getId());
        }
    }
}
