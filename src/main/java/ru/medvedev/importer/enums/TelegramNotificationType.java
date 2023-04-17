package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;

public enum TelegramNotificationType {
    ALL, //все нотификации
    ADDING_ERROR //ошибки добавления
}
