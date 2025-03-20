package ru.medvedev.importer.service.export.tablebuilder;

import org.springframework.stereotype.Component;
import ru.medvedev.importer.dto.ColumnInfo;
import ru.medvedev.importer.entity.CheckLeadInfoEntity;
import ru.medvedev.importer.service.export.service.BaseTableBuilder;

@Component
public class CheckLeadResultTableBuilder extends BaseTableBuilder<CheckLeadInfoEntity> {

    @Override
    public void generateTable() {
        addNewColumn("inn", ColumnInfo.of("ИНН", CheckLeadInfoEntity::getInn));
    }
}
