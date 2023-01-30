package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookType;

@Data
public class WebhookSuccessStatusDto {

    private Long id;
    private String name;
    private Long statusId;
    private String bankName;
    private Bank bank;
    private WebhookType type;

    public static WebhookSuccessStatusDto of(WebhookSuccessStatusEntity entity) {
        WebhookSuccessStatusDto dto = new WebhookSuccessStatusDto();
        dto.setId(entity.getId());
        dto.setName(entity.getWebhookStatusEntity().getName());
        dto.setBank(entity.getBank());
        dto.setBankName(entity.getBank().getTitle());
        return dto;
    }
}
