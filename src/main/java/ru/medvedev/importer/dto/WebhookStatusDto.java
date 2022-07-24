package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.WebhookStatusEntity;

@Data
public class WebhookStatusDto {

    private Long id;
    private String name;

    public static WebhookStatusDto of(WebhookStatusEntity entity) {
        WebhookStatusDto dto = new WebhookStatusDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}
