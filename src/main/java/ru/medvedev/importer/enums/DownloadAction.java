package ru.medvedev.importer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum DownloadAction {

    ACTION_LEAD_DOWNLOAD,
    ACTION_WEBHOOK_DOWNLOAD
}
