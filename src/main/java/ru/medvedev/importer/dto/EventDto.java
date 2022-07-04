package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.EventEntity;

import java.time.format.DateTimeFormatter;

@Data
public class EventDto {

    private Long id;
    private String createAt;
    private String type;
    private String description;

    public static EventDto of(EventEntity entity) {
        EventDto dto = new EventDto();
        dto.setId(entity.getId());
        dto.setType(entity.getType().getDescription());
        dto.setCreateAt(entity.getCreateAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
