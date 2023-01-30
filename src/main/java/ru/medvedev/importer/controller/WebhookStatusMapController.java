package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.WebhookStatusMapDto;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.WebhookStatusMapService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/settings/webhook-status-map")
@RequiredArgsConstructor
public class WebhookStatusMapController {

    private final WebhookStatusMapService service;
    private final WebhookSuccessStatusService statusService;

    @GetMapping
    public String getPage(Model model) {

        List<WebhookStatusMapDto> statusDtoList = service.getList().stream()
                .map(WebhookStatusMapDto::of)
                .collect(Collectors.toList());

        model.addAttribute("data", statusDtoList);
        model.addAttribute("banks", Bank.values());
        model.addAttribute("statuses", statusService.findWebhookStatusByType(Collections.singletonList(WebhookType.SUCCESS)));
        model.addAttribute("actualities", ContactActuality.values());

        model.addAttribute("authority", getAuthorityList());
        return "webhook_status_map";
    }

    @PostMapping
    public String create(@RequestBody WebhookStatusMapDto input) {
        service.create(input);
        return "redirect:/settings/webhook-status-map";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
