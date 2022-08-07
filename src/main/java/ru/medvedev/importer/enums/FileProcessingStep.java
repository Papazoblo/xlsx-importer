package ru.medvedev.importer.enums;


public enum FileProcessingStep {

    INITIALIZE,
    REQUEST_COLUMN_NAME,
    RESPONSE_COLUMN_NAME,
    REQUEST_REQUIRE_FIELD,
    RESPONSE_REQUIRE_FIELD,
    WAIT_READ_DATA,
    READ_DATA
}
