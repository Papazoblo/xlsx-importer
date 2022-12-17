package ru.medvedev.importer.service.sheethandler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.model.SharedStringsTable;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.repository.ContactNewRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static feign.Util.isNotBlank;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class ContactNewSheetHandler extends BaseItemSheetHandler {

    @Getter
    private final List<ContactNewEntity> contacts = new ArrayList<>();
    @Getter
    private final Set<String> innList = new HashSet<>();

    private final ContactNewRepository contactNewRepository;

    public ContactNewSheetHandler(SharedStringsTable sst, ContactNewRepository contactNewRepository) {
        super(sst);
        this.contactNewRepository = contactNewRepository;
    }

    @Override
    protected int getStartIndexRowData() {
        return 3;
    }

    @Override
    protected void createItem() {

        String value = rowMap.get("B");
        if (isNotBlank(value)) {

            innList.add(value);

            ContactNewEntity contactNewEntity;
            if(contactNewRepository.existsByInn(value)) {
                return;
            } else {
                contactNewEntity = new ContactNewEntity();
            }

            contactNewEntity.setInn(value);

            setValue("A", contactNewEntity::setOgrn, true);
            setValue("C", contactNewEntity::setSurname);
            setValue("D", contactNewEntity::setName);
            setValue("E", contactNewEntity::setMiddleName, true);
            setValue("F", contactNewEntity::setPhone);
            setValue("G", contactNewEntity::setOrgName);
            setValue("H", contactNewEntity::setCity);
            setValue("I", contactNewEntity::setRegion, true);

            if (isBlank(contactNewEntity.getSurname())
                    || isBlank(contactNewEntity.getName())
                    || isBlank(contactNewEntity.getPhone())
                    || isBlank(contactNewEntity.getOrgName())
                    || isBlank(contactNewEntity.getCity())) {
                log.debug("Invalid import contact {}", contactNewEntity);
            } else {
                contacts.add(contactNewEntity);
            }
        }
    }
}
