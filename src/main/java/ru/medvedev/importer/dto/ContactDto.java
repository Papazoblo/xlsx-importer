package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.entity.ContactBankActualityEntity;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    private String city;
    private String vtbStatus;
    private String openingStatus;
    private String vtbActuality;
    private String openingActuality;
    private String createAt;

    public static ContactDto of(ContactNewEntity entity) {
        ContactDto dto = new ContactDto();
        dto.setId(entity.getId());
        dto.setInn(entity.getInn());
        dto.setFio(getFioStringFromContact(entity));
        dto.setPhone(entity.getPhone());
        dto.setOrgName(entity.getOrgName());
        dto.setRegion(entity.getRegion());
        dto.setCity(entity.getCity());
        Arrays.stream(Bank.values()).forEach(bank -> {
            Optional<ContactBankActualityEntity> actualityEntity = entity.getActualityList().stream()
                    .filter(actuality -> actuality.getBank() == bank)
                    .findFirst();

            switch (bank) {
                case VTB:
                    if (actualityEntity.isPresent()) {
                        dto.setVtbStatus(actualityEntity.get().getWebhookStatusEntity().getName());
                        dto.setVtbActuality(actualityEntity.get().getActuality().getTitle());
                    } else {
                        dto.setVtbStatus("");
                        dto.setVtbActuality(ContactActuality.NEW.getTitle());
                    }
                    break;
                case VTB_OPENING:
                    if (actualityEntity.isPresent()) {
                        dto.setOpeningStatus(actualityEntity.get().getWebhookStatusEntity().getName());
                        dto.setOpeningActuality(actualityEntity.get().getActuality().getTitle());
                    } else {
                        dto.setOpeningStatus("");
                        dto.setOpeningActuality(ContactActuality.NEW.getTitle());
                    }
                    break;
            }
        });
        dto.setCreateAt(entity.getCreateAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        return dto;
    }
}
