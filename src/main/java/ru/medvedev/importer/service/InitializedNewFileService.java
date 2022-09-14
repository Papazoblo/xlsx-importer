package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.ProjectCodeResponseEvent;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.SystemVariable;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.medvedev.importer.enums.ChatState.NONE;
import static ru.medvedev.importer.enums.ChatState.PROJECT_CODE;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitializedNewFileService {

    private final FileInfoService fileInfoService;
    private final TelegramPollingService telegramService;
    private final ProjectNumberService projectNumberService;
    private final SystemVariableService systemVariableService;
    private final ApplicationEventPublisher eventPublisher;


    @Scheduled(cron = "${cron.tg-initialized-new-file}")
    public void sendRequestToTelegram() {
        fileInfoService.getTgFileToSetProjectCode().ifPresent(file -> {

            systemVariableService.save(SystemVariable.CHAT_STATE, PROJECT_CODE.name());
            file.setProcessingStep(FileProcessingStep.WAIT_PROJECT_CODE_INITIALIZE);
            fileInfoService.save(file);
            List<String> buttons = new ArrayList<>();
            fileInfoService.getLastTgFileProjectCode().ifPresent(s ->
                    buttons.add("Последняя загрузка с интерфейса: " + s));
            Optional.ofNullable(projectNumberService.getNumberByDate(LocalDate.now())).ifPresent(value ->
                    buttons.add("Проект для загрузки через бота: " + value));
            telegramService.sendRequestGetProjectCode(file.getName(), buttons);
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
                file.setProjectId(String.valueOf(Long.parseLong(value)));
                file.setProcessingStep(FileProcessingStep.RESPONSE_COLUMN_NAME);
                fileInfoService.save(file);
                systemVariableService.save(SystemVariable.CHAT_STATE, NONE.name());
            } catch (NumberFormatException ex) {
                eventPublisher.publishEvent(new ImportEvent(this, "Неверный формат кода проекта",
                        EventType.LOG_TG, file.getId()));
            }
        });
    }
}
