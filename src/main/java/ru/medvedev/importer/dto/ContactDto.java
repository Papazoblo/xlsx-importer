package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.enums.Bank;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static ru.medvedev.importer.utils.StringUtils.getFioStringFromContact;

@Data
public class ContactDto {

    private Long id;
    private String inn;
    private String fio;
    private String phone;
    private String orgName;
    private String region;
    private String createAt;
    private Bank bank;
    private String status;
    private String original;
    private String webhookStatus;

    public static ContactDto of(ContactEntity entity, boolean original) {
        ContactDto dto = new ContactDto();
        dto.setId(entity.getId());
        dto.setInn(entity.getInn());
        dto.setFio(getFioStringFromContact(entity));
        dto.setPhone(entity.getPhone());
        dto.setOrgName(entity.getOrgName());
        dto.setRegion(entity.getRegion());
        dto.setBank(entity.getBank());
        dto.setCreateAt(entity.getCreateAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        dto.setStatus(entity.getStatus().getDescription());
        dto.setOriginal(original ? "+" : "");
        dto.setWebhookStatus(Optional.ofNullable(entity.getWebhookStatus())
                .map(WebhookStatusEntity::getName).orElse(""));
        return dto;
    }
}
