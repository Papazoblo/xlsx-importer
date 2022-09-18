package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.dto.WebhookSuccessStatusDto;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@RequiredArgsConstructor
@Controller
@RequestMapping("/settings/webhook-success-statuses")
public class WebhookSuccessStatusController {

    private final WebhookSuccessStatusService service;

    @GetMapping
    public String getPageProject(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("name")));

        Page<WebhookSuccessStatusDto> resultPage = service.getPage(pageable);
        model.addAttribute("data", resultPage);
        model.addAttribute("banks", Bank.values());
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "webhook_success_status";
    }

    @PostMapping
    public String create(@RequestBody WebhookSuccessStatusDto input) {
        service.create(input);
        return "redirect:/settings/webhook-success-statuses";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
