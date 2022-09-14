package ru.medvedev.importer.enums;


public enum FileProcessingStep {

    IN_QUEUE, //
    WAIT_PROJECT_CODE_INITIALIZE,
    INITIALIZE,
    REQUEST_COLUMN_NAME,
    RESPONSE_COLUMN_NAME,
    REQUEST_REQUIRE_FIELD,
    RESPONSE_REQUIRE_FIELD,
    WAIT_READ_DATA,
    READ_DATA
}
