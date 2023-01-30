package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.WebhookStatusMapEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;

@Data
public class WebhookStatusMapDto {

    private Long id;
    private WebhookStatusDto webhookStatus;
    private Integer priority;
    private ContactActuality actuality;
    private Bank bank;
    private Integer errorCount;

    public static WebhookStatusMapDto of(WebhookStatusMapEntity entity) {
        WebhookStatusMapDto dto = new WebhookStatusMapDto();
        dto.setId(entity.getId());
        dto.setActuality(entity.getActuality());
        dto.setBank(entity.getBank());
        dto.setErrorCount(entity.getErrorCount());
        dto.setPriority(entity.getPriority());
        dto.setWebhookStatus(WebhookStatusDto.of(entity.getWebhookStatus()));
        return dto;
    }
}
