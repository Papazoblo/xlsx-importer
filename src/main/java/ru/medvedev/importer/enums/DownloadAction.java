package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum DownloadAction {

    ACTION_LEAD_DOWNLOAD, //загрузка в скорозвон
    ACTION_WEBHOOK_DOWNLOAD, //загрузка вебхуков
    ACTION_SYSTEM_DOWNLOAD, //просто загрузка в систему
    ACTION_ALFA_SYSTEM_DOWNLOAD; //загрузка в систему для альфы, без очереди

}
