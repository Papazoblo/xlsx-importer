package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.enums.ContactActuality;

import java.util.Optional;

@Data
public class ContactReloadDto {

    private String ogrn;
    private String inn;
    private String surname;
    private String name;
    private String middleName;
    private String phone;
    private String orgName;
    private String region;
    private String city;
    private String vtbState;
    private ContactActuality vtbActuality;
    private String openingState;
    private ContactActuality openingActuality;

    public static ContactReloadDto of(ContactNewEntity entity) {
        ContactReloadDto dto = new ContactReloadDto();
        dto.setOgrn(entity.getOgrn());
        dto.setInn(entity.getInn());
        dto.setSurname(entity.getSurname());
        dto.setName(entity.getName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setPhone(entity.getPhone());
        dto.setOrgName(entity.getOrgName());
        dto.setRegion(entity.getRegion());
        dto.setCity(entity.getCity());
        entity.getActualityList().forEach(item -> {
            switch (item.getBank()) {
                case VTB_OPENING:
                    dto.setOpeningActuality(item.getActuality());
                    dto.setOpeningState(Optional.ofNullable(item.getWebhookStatusEntity()).map(WebhookStatusEntity::getName).orElse(""));
                    break;
                case VTB:
                    dto.setVtbActuality(item.getActuality());
                    dto.setVtbState(Optional.ofNullable(item.getWebhookStatusEntity()).map(WebhookStatusEntity::getName).orElse(""));
                    break;
            }
        });
        return dto;
    }
}
