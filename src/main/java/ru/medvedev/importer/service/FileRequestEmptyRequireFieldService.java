package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.FileRequestEmptyRequireFieldEntity;
import ru.medvedev.importer.enums.XlsxRequireField;

@Service
@RequiredArgsConstructor
public class FileRequestEmptyRequireFieldService {

    public FileRequestEmptyRequireFieldEntity createRequest(FileInfoEntity file, String columnName) {
        FileRequestEmptyRequireFieldEntity entity = new FileRequestEmptyRequireFieldEntity();
        entity.setColumn(XlsxRequireField.of(columnName));
        entity.setFileInfoEntity(file);
        return entity;
    }

}
