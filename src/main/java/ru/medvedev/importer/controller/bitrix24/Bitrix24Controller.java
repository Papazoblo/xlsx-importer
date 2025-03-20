package ru.medvedev.importer.controller.bitrix24;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bitrix24")
@Slf4j
public class Bitrix24Controller {

    @PostMapping("/webhook")
    public void takeWebhook(@RequestBody String body) {
        log.debug("*** Take a webhook bitrix24: {}", body);
    }
}
