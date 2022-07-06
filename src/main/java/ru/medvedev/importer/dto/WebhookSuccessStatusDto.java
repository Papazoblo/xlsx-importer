package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;

@Data
public class WebhookSuccessStatusDto {

    private Long id;
    private String name;

    public static WebhookSuccessStatusDto of(WebhookSuccessStatusEntity entity) {
        WebhookSuccessStatusDto dto = new WebhookSuccessStatusDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
