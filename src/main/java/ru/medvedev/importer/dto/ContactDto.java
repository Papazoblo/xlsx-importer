package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.ContactEntity;

import java.time.format.DateTimeFormatter;

@Data
public class ContactDto {

    private Long id;
    private String inn;
    private String fio;
    private String phone;
    private String orgName;
    private String region;
    private String createAt;
    private String status;
    private String original;

    public static ContactDto of(ContactEntity entity, boolean original) {
        ContactDto dto = new ContactDto();
        dto.setId(entity.getId());
        dto.setInn(entity.getInn());
        dto.setFio(String.format("%s %s %s",
                entity.getSurname(), entity.getName(), entity.getMiddleName()).trim());
        dto.setPhone(entity.getPhone());
        dto.setOrgName(entity.getOrgName());
        dto.setRegion(entity.getRegion());
        dto.setCreateAt(entity.getCreateAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        dto.setStatus(entity.getStatus().getDescription());
        dto.setOriginal(original ? "+" : "");
        return dto;
    }
}
