package ru.medvedev.importer.service.sheethandler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.model.SharedStringsTable;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.enums.WebhookStatus;
import ru.medvedev.importer.repository.ContactNewRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static feign.Util.isNotBlank;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class WebhookSheetHandler extends BaseItemSheetHandler {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyyHH:mm");

    @Getter
    private final List<WebhookDto> webhooks = new ArrayList<>();
    private final Map<String, ScenarioDto> scenarioNameMap;

    public WebhookSheetHandler(SharedStringsTable sst, Map<String, ScenarioDto> scenarioNameMap) {
        super(sst);
        this.scenarioNameMap = scenarioNameMap;
    }

    @Override
    protected int getStartIndexRowData() {
        return 2;
    }

    @Override
    protected void createItem() {

        String value = rowMap.get("A");
        if (isNotBlank(value)) {

            WebhookDto webhookDto = new WebhookDto();
            WebhookLeadDto leadDto = new WebhookLeadDto();
            WebhookContactDto contactDto = new WebhookContactDto();
            CallResultDto callResultDto = new CallResultDto();
            CallDto callDto = new CallDto();


            leadDto.setCreateAt(LocalDateTime.parse(value + rowMap.get("B"), DATE_TIME_FORMATTER)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss")));
            setValue("AB", leadDto::setInn);
            setValue("O", item -> leadDto.setEmails(Optional.ofNullable(item)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList())));
            setValue("S", leadDto::setPhones);
            setValue("AC", leadDto::setCity, true);
            setValue("E", leadDto::setName);
            setValue("Z", callResultDto::setComment, true);
            setValue("X", callResultDto::setResultName);
            setValue("F", contactDto::setName);

            callDto.setScenarioId(Optional.ofNullable(scenarioNameMap.get(rowMap.get("D").trim()))
                    .map(ScenarioDto::getId).orElse(0L));
            webhookDto.setType("call_result");
            webhookDto.setCallResult(callResultDto);
            webhookDto.setContact(contactDto);
            webhookDto.setLead(leadDto);

            webhooks.add(webhookDto);
        }
    }
}
