package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.dto.FieldPositionDto;
import ru.medvedev.importer.dto.HeaderDto;
import ru.medvedev.importer.dto.events.CheckBotColumnResponseEvent;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.FileRequestEmptyRequireFieldEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.telegram.TelegramPollingService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeaderColumnAssignmentService {

    private final TelegramPollingService telegramService;
    private final FileInfoService fileInfoService;
    private final FieldNameVariantService fieldNameVariantService;
    private final FileRequestEmptyRequireFieldService fileRequestEmptyRequireFieldService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${cron.tg-column-header-request}")
    public void sendRequestToTelegram() {
        fileInfoService.getFileToTgRequest().ifPresent(this::findColumnToAsk);
    }

    @EventListener(CheckBotColumnResponseEvent.class)
    public void checkBotColumnResponseEventListener(CheckBotColumnResponseEvent event) {
        fileInfoService.getFileWaitColumnResponse().ifPresent(file -> file.getColumnInfo().ifPresent(columnInfo -> {
            Optional.ofNullable(file.getAskColumnNumber()).ifPresent(columnPosition -> {
                try {
                    XlsxRequireField xlsxField = XlsxRequireField.of(event.getText());
                    FieldPositionDto fieldPositionDto = columnInfo.getFieldPositionMap().get(xlsxField);
                    if (fieldPositionDto == null) {
                        fieldPositionDto = new FieldPositionDto();
                        fieldPositionDto.setRequired(true);
                    }
                    HeaderDto headerDto = new HeaderDto();
                    headerDto.setPosition(columnPosition);
                    headerDto.setValue(columnInfo.getColumnInfoMap().get(columnPosition).get(0));
                    fieldPositionDto.getHeader().add(headerDto);
                    columnInfo.getFieldPositionMap().put(xlsxField, fieldPositionDto);
                    file.setColumnInfo(columnInfo);
                    file.setProcessingStep(FileProcessingStep.RESPONSE_COLUMN_NAME);
                    file.setAskColumnNumber(null);
                } catch (Exception ex) {
                    log.debug("Error convert text to XlsxRequireField");
                }
            });
            fileInfoService.save(file);
        }));

        fileInfoService.getFileWaitRequireColumnResponse().ifPresent(file -> {
            file.getFileRequestEmptyRequireFieldEntities().stream()
                    .filter(request -> !request.getHaveAnswer())
                    .findFirst().ifPresent(requestEntity -> file.getColumnInfo().ifPresent(columnInfo -> {
                if (!event.getText().equals("Пропустить")) {
                    HeaderDto headerDto = new HeaderDto();
                    headerDto.setValue(event.getText());
                    headerDto.setPosition(event.getPosition());
                    FieldPositionDto fieldPositionDto = new FieldPositionDto();
                    fieldPositionDto.setHeader(Collections.singletonList(headerDto));
                    columnInfo.getFieldPositionMap().put(requestEntity.getColumn(), fieldPositionDto);
                    file.setColumnInfo(columnInfo);
                }
                file.getFileRequestEmptyRequireFieldEntities().forEach(item -> item.setHaveAnswer(true));
                file.setProcessingStep(FileProcessingStep.RESPONSE_REQUIRE_FIELD);
                fileInfoService.save(file);
            }));
        });
    }

    private void findColumnToAsk(FileInfoEntity file) {
        file.getColumnInfo().ifPresent(columnInfo -> {
            //ищем номер столбца, которого нет ни в одном HeaderDto
            Optional<Integer> optionalPosition = columnInfo.getColumnInfoMap().keySet().stream().filter(columnNum ->
                    columnInfo.getFieldPositionMap().values().stream()
                            .flatMap(fieldPositionDto -> fieldPositionDto.getHeader().stream())
                            .noneMatch(header -> header.getPosition().equals(columnNum))
            ).findFirst();

            //если такой номер есть, то формируем ТГ сообщение
            if (optionalPosition.isPresent()) {
                telegramService.sendRequestGetColumnName(file.getName(),
                        getEmptyRequiredColumn(columnInfo.getFieldPositionMap()),
                        columnInfo.getColumnInfoMap().get(optionalPosition.get()));
                file.setProcessingStep(FileProcessingStep.REQUEST_COLUMN_NAME);
                file.setAskColumnNumber(optionalPosition.get());
            } else {//иначе переводим файл в статус WAIT_READ_DATA
                //todo добавить условие , что обязательные поля заполнены
                List<String> emptyRequiredFieldList = getEmptyRequireColumnWithNoRequest(file,
                        columnInfo.getFieldPositionMap());
                if (emptyRequiredFieldList.isEmpty()) {
                    //проверяем, Что все обязательные поля заполнены, иначе кидаем фаил в ошибку
                    if (getEmptyRequiredColumn(columnInfo.getFieldPositionMap()).isEmpty()) {
                        file.setProcessingStep(FileProcessingStep.WAIT_READ_DATA);
                    } else {
                        eventPublisher.publishEvent(new ImportEvent(this, "Для файла указаны не все обязательные поля",
                                EventType.ERROR, file.getId()));
                        return;
                    }
                } else {
                    //говорим, Что поле не заполнено и предлагаем столбцы
                    //делаем клавиатуру из значений столбцов
                    //если у файла есть шапка, то название столбца
                    //иначе первые две строчки
                    FileRequestEmptyRequireFieldEntity fileRequestEntity = fileRequestEmptyRequireFieldService.createRequest(file,
                            emptyRequiredFieldList.get(0));
                    file.getFileRequestEmptyRequireFieldEntities().add(fileRequestEntity);
                    file.setProcessingStep(FileProcessingStep.REQUEST_REQUIRE_FIELD);
                    telegramService.sendRequestGetRequireColumn(file, emptyRequiredFieldList.get(0));
                }
            }
            fileInfoService.save(file);
        });
    }

    private List<String> getEmptyRequiredColumn(Map<XlsxRequireField, FieldPositionDto> requiredFields) {
        return fieldNameVariantService.getAll().values().stream()
                .filter(FieldNameVariantDto::isRequired)
                .filter(fieldInfo -> fieldInfo.getField() != XlsxRequireField.TRASH)
                .filter(fieldInfo -> {
                    FieldPositionDto fieldPositionDto = requiredFields.get(fieldInfo.getField());
                    return fieldPositionDto == null;
                })
                .map(fieldInfo -> fieldInfo.getField().getDescription())
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getEmptyRequireColumnWithNoRequest(FileInfoEntity file, Map<XlsxRequireField, FieldPositionDto> requireFields) {
        List<String> emptyRequireFieldList = getEmptyRequiredColumn(requireFields);
        return emptyRequireFieldList.stream()
                .filter(field -> file.getFileRequestEmptyRequireFieldEntities().stream()
                        .noneMatch(request -> request.getColumn() == XlsxRequireField.of(field)))
                .collect(Collectors.toList());
    }
}
