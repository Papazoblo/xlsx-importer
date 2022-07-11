package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    DOWNLOAD_XLSX("Возможность загружать xlsx"),
    FILE_STORAGE("Просмотр загруженных файлов"),
    CONTACTS("Просмотр контактов"),
    EVENTS("Просмотр событий"),
    COLUMN_NAME("Возможность настраивать названия колонок"),
    DOWNLOADS_PROJECT("Возможность редактировать список проектов для загрузки контактов"),
    WEBHOOK_STATUS("Возможность редактировать список статусов для вебхуков"),
    USERS("Возможность редактирования списка пользователей");

    @Getter
    private final String description;
}
