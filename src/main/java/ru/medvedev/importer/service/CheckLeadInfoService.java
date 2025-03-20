package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.medvedev.importer.dto.events.SendResultCheckExportEvent;
import ru.medvedev.importer.entity.CheckLeadInfoEntity;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.ExportType;
import ru.medvedev.importer.repository.CheckLeadInfoRepository;
import ru.medvedev.importer.service.export.exporter.CheckLeadResultExporterService;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CheckLeadInfoService {

    private final CheckLeadInfoRepository repository;
    private final CheckLeadResultExporterService checkLeadResultExporterService;
    private final TelegramPollingService telegramPollingService;

    @Transactional
    public void save(List<CheckLeadInfoEntity> entityToSave) {
        repository.saveAll(entityToSave);
    }

    @Transactional
    public void saveInBatch(List<CheckLeadInfoEntity> entityToSave, List<FileInfoBankEntity> fileInfoBankList) {

        fileInfoBankList.forEach(fileInfoBank ->
                entityToSave.forEach(checkLead ->
                        checkLead.setFileInfoBankId(fileInfoBank.getId())));
        repository.saveAll(entityToSave);
    }

    @EventListener
    public void sendResultExportToTG(SendResultCheckExportEvent event) {
        try {
            telegramPollingService.sendDocument(checkLeadResultExporterService.exporting(ExportType.XLSX, event.getFileInfo().getLeadToCheckList().stream()
                    .filter(checkLeadInfoEntity -> checkLeadInfoEntity.getCheckStatus() == ContactStatus.DOWNLOADED)
                    .collect(Collectors.toList())).getInputStream(), event.getFileInfo().getName(), event.getFileInfo().getChatId().toString(), event.getFileInfo().getMessageId());
        } catch (IOException ioException) {
            log.debug("Error export check lead result");
        }
    }
}
