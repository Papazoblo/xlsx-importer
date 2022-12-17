package ru.medvedev.importer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.medvedev.importer.entity.WebhookStatusEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
