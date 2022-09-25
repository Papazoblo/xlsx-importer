package ru.medvedev.importer.service.export.tablebuilder;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.dto.ColumnInfo;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.service.export.service.BaseTableBuilder;

import java.util.Optional;

@Component
public class ContactTableBuilder extends BaseTableBuilder<ContactEntity> {

    @Override
    public void generateTable() {
        addTitle("Выгрузка контактов");
        addNewColumn("surname", ColumnInfo.of("Фамилия", ContactEntity::getSurname));
        addNewColumn("name", ColumnInfo.of("Имя", ContactEntity::getName));
        addNewColumn("middleName", ColumnInfo.of("Отчество", ContactEntity::getMiddleName));
        addNewColumn("inn", ColumnInfo.of("ИНН", ContactEntity::getInn));
        addNewColumn("ogrn", ColumnInfo.of("ОГРН", ContactEntity::getOgrn));
        addNewColumn("phone", ColumnInfo.of("Телефон", ContactEntity::getPhone));
        addNewColumn("city", ColumnInfo.of("Город", ContactEntity::getCity));
        addNewColumn("region", ColumnInfo.of("Регион", ContactEntity::getRegion));
        addNewColumn("orgName", ColumnInfo.of("Организация", ContactEntity::getOrgName));
        addNewColumn("status", ColumnInfo.of("Статус", item -> item.getStatus().getDescription()));
        addNewColumn("createAt", ColumnInfo.of("Дата создания", item -> item.getCreateAt().toString()));
        addNewColumn("bank", ColumnInfo.of("Банк", item -> Optional.ofNullable(item.getBank()).map(Bank::getTitle).orElse("")));
    }
}
