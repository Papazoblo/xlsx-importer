package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoLoadScheduler {

    private final ContactService contactService;
    private final AutoLoadService autoLoadService;

    /*@Scheduled(cron = "${cron.auto-load}")
    public void launchLoad() {

    }*/
}
