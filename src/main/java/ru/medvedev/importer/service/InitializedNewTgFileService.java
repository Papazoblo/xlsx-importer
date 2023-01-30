package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.events.BankSelectResponseEvent;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.ProjectCodeResponseEvent;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.SystemVariable;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static ru.medvedev.importer.enums.ChatState.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitializedNewTgFileService {

    private final FileInfoService fileInfoService;
    private final TelegramPollingService telegramService;
    private final ProjectNumberService projectNumberService;
    private final SystemVariableService systemVariableService;
    private final ApplicationEventPublisher eventPublisher;


    @Scheduled(cron = "${cron.tg-initialized-new-file}")
    public void requestToSelectBank() {
        fileInfoService.getTgFileToSelectBank().ifPresent(file -> {

            systemVariableService.save(SystemVariable.CHAT_STATE, BANK_SELECT.name());
            file.setProcessingStep(FileProcessingStep.WAIT_BANK_INITIALIZE);
            fileInfoService.save(file);
            sendRequestSelectBank(file);
        });
    }

    private void sendRequestSelectBank(FileInfoEntity fileInfo) {
        List<String> buttons = Arrays.stream(Bank.values())
                .filter(item -> fileInfo.getBankList().stream()
                        .noneMatch(bankEntity -> bankEntity.getBank() == item))
                .map(Bank::getTitle)
                .collect(toList());
        buttons.add("Продолжить");
        telegramService.sendRequestToSelectBank(fileInfo.getName(), buttons);
    }

    @Scheduled(cron = "${cron.tg-initialized-new-file}")
    public void sendRequestToTelegram() {
        fileInfoService.getTgFileToSetProjectCode().ifPresent(file -> {

            systemVariableService.save(SystemVariable.CHAT_STATE, PROJECT_CODE.name());
            file.setProcessingStep(FileProcessingStep.WAIT_PROJECT_CODE_INITIALIZE);
            fileInfoService.save(file);
            List<String> buttons = new ArrayList<>();
            Bank bank = file.getBankList().stream()
                    .filter(item -> item.getProjectId().equals(-1L))
                    .findFirst()
                    .map(FileInfoBankEntity::getBank)
                    .orElse(null);
            fileInfoService.getLastTgFileProjectCode(bank).forEach(s ->
                    buttons.add("Последняя загрузка с интерфейса `" + s.getBank().getTitle() + "`: " + s.getProjectId()));
            Optional.ofNullable(projectNumberService.getNumberByDate(bank, LocalDate.now())).ifPresent(value ->
                    buttons.add("Проект для загрузки через бота: " + value));
            telegramService.sendRequestGetProjectCode(Optional.ofNullable(bank).map(Bank::getTitle).orElse(""),
                    file.getName(), buttons);
        });
    }

    @EventListener(ProjectCodeResponseEvent.class)
    public void projectCodeResponseListener(ProjectCodeResponseEvent event) {
        fileInfoService.getFileWaitProjectCode().ifPresent(file -> {
            if (event.getText().equalsIgnoreCase("Отменить загрузку")) {
                systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
                eventPublisher.publishEvent(new ImportEvent(this, "Загрузка файла отменена",
                        EventType.ERROR, file.getId()));
                return;
            }
            String value = event.getText();
            String[] splitValue = value.split(":");
            value = (splitValue.length == 2 ? splitValue[1] : splitValue[0]).trim();
            try {
                for (FileInfoBankEntity fi : file.getBankList()) {
                    if (fi.getProjectId().equals(-1L)) {
                        fi.setProjectId(Long.parseLong(value));
                    }
                }
                if (Arrays.stream(Bank.values()).allMatch(bank ->
                        file.getBankList().stream().anyMatch(bankEntity -> bankEntity.getBank() == bank))) {
                    file.setProcessingStep(FileProcessingStep.RESPONSE_COLUMN_NAME);
                } else {
                    file.setProcessingStep(FileProcessingStep.INITIALIZE);
                }
                fileInfoService.save(file);
                systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
            } catch (NumberFormatException ex) {
                eventPublisher.publishEvent(new ImportEvent(this, "Неверный формат кода проекта",
                        EventType.LOG_TG, file.getId()));
            }
        });
    }

    @EventListener(BankSelectResponseEvent.class)
    public void bankSelectResponseListener(BankSelectResponseEvent event) {
        fileInfoService.getFileWaitBankInitialize().ifPresent(file -> {
            if (event.getText().equalsIgnoreCase("Отменить загрузку")) {
                systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
                eventPublisher.publishEvent(new ImportEvent(this, "Загрузка файла отменена",
                        EventType.ERROR, file.getId()));
                return;
            }
            if (event.getText().equalsIgnoreCase("Продолжить")) {
                if (file.getBankList().isEmpty()) {
                    eventPublisher.publishEvent(new ImportEvent(this, "Не выбран ни один банк",
                            EventType.LOG_TG, file.getId()));
                    sendRequestSelectBank(file);
                    return;
                } else {
                    file.setProcessingStep(FileProcessingStep.RESPONSE_COLUMN_NAME);
                    fileInfoService.save(file);
                    systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
                    return;
                }
            }
            try {
                Bank selectedBank = Bank.of(event.getText());
                if (file.getBankList().isEmpty() || file.getBankList().stream()
                        .noneMatch(item -> item.getBank() == selectedBank)) {
                    FileInfoBankEntity fileBank = new FileInfoBankEntity();
                    fileBank.setBank(selectedBank);
                    fileBank.setFileInfo(file);
                    file.getBankList().add(fileBank);
                }
                file.setProcessingStep(FileProcessingStep.BANK_INITIALIZED);
                fileInfoService.save(file);
                systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
            } catch (Exception ex) {
                sendRequestSelectBank(file);
            }
        });
    }
}
