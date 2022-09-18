package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.Bank;

@Data
public class WebhookSuccessStatusDto {

    private Long id;
    private String name;
    private String bankName;
    private Bank bank;

    public static WebhookSuccessStatusDto of(WebhookSuccessStatusEntity entity) {
        WebhookSuccessStatusDto dto = new WebhookSuccessStatusDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBank(entity.getBank());
        dto.setBankName(entity.getBank().getTitle());
        return dto;
    }
}
