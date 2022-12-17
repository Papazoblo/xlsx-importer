package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.WebhookStatusDto;
import ru.medvedev.importer.service.WebhookStatusService;

import java.util.Comparator;
import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/settings/webhook-statuses")
@RequiredArgsConstructor
public class WebhookStatusController {

    private final WebhookStatusService service;

    @GetMapping
    public String getPage(Model model) {

        List<WebhookStatusDto> statusDtoList = service.getAll();
        statusDtoList.sort(Comparator.comparing(WebhookStatusDto::getName));

        model.addAttribute("statuses", statusDtoList);
        model.addAttribute("authority", getAuthorityList());
        return "webhook_status";
    }

    @PostMapping
    public String create(@RequestBody WebhookStatusDto input) {
        service.creatIfNotExists(input.getName());
        return "redirect:/settings/webhook-statuses";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
