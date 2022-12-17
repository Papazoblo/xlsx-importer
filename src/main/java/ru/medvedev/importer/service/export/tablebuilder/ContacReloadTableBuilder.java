package ru.medvedev.importer.service.export.tablebuilder;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.dto.ColumnInfo;
import ru.medvedev.importer.dto.ContactReloadDto;
import ru.medvedev.importer.enums.ContactActuality;
import ru.medvedev.importer.service.export.service.BaseTableBuilder;

import java.util.Optional;

@Component
public class ContacReloadTableBuilder extends BaseTableBuilder<ContactReloadDto> {

    @Override
    public void generateTable() {
        addNewColumn("ogrn", ColumnInfo.of("ОГРН", ContactReloadDto::getOgrn));
        addNewColumn("inn", ColumnInfo.of("ИНН", ContactReloadDto::getInn));
        addNewColumn("surname", ColumnInfo.of("Фамилия", ContactReloadDto::getSurname));
        addNewColumn("name", ColumnInfo.of("Имя", ContactReloadDto::getName));
        addNewColumn("middleName", ColumnInfo.of("Отчество", ContactReloadDto::getMiddleName));
        addNewColumn("phone", ColumnInfo.of("Телефон", ContactReloadDto::getPhone));
        addNewColumn("orgName", ColumnInfo.of("Сокращенное наименование", ContactReloadDto::getOrgName));
        addNewColumn("city", ColumnInfo.of("Город", ContactReloadDto::getCity));
        addNewColumn("region", ColumnInfo.of("Регион", ContactReloadDto::getRegion));
        addNewColumn("vtbState", ColumnInfo.of("Статус лида ВТБ", ContactReloadDto::getVtbState));
        addNewColumn("openingState", ColumnInfo.of("Статус лида Открытие", ContactReloadDto::getOpeningState));
        addNewColumn("vtbActuality", ColumnInfo.of("Актуальность ВТБ", item ->
                Optional.ofNullable(item.getVtbActuality()).map(ContactActuality::getTitle).orElse("")));
        addNewColumn("openingActuality", ColumnInfo.of("Актуальность Открытие", item ->
                Optional.ofNullable(item.getOpeningActuality()).map(ContactActuality::getTitle).orElse("")));

    }
}
