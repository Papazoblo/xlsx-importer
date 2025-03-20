package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DownloadAction {

    ACTION_LEAD_DOWNLOAD("main"), //загрузка в скорозвон
    ACTION_WEBHOOK_DOWNLOAD("main"), //загрузка вебхуков
    ACTION_SYSTEM_DOWNLOAD("main"), //просто загрузка в систему
    ACTION_LEAD_BANK_CHECK("lead_check"), //проверка контактов в альфе и выдача результата в бота
    ACTION_ALFA_SYSTEM_DOWNLOAD("alfa_main"); //загрузка в систему для альфы, без очереди

    private final String template;
}
