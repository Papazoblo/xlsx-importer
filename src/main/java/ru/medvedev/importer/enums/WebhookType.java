package ru.medvedev.importer.enums;

public enum WebhookType {

    ERROR,
    SUCCESS, //для обработки контакта
    MINOR,
    CREATE_REQUEST // для создания заявки
}
