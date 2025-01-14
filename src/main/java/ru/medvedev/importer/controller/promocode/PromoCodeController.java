package ru.medvedev.importer.controller.promocode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promo-code")
@Slf4j
public class PromoCodeController {

    @PostMapping("/amo-crm/webhook")
    public void takeWebhook(@RequestBody String body) {
        log.debug("*** Take a webhook amo: {}", body);
    }
}
