package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.dto.WebhookStatusDto;
import ru.medvedev.importer.dto.WebhookSuccessFilter;
import ru.medvedev.importer.dto.WebhookSuccessStatusDto;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.WebhookStatusService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

import java.util.Comparator;
import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@RequiredArgsConstructor
public abstract class BaseWebhookStatusController {

    private final WebhookStatusService statusService;
    private final WebhookSuccessStatusService service;

    @GetMapping
    public String getPageProject(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.asc("webhookStatusEntity.name")));
        WebhookSuccessFilter filter = new WebhookSuccessFilter();
        filter.setType(getWebhookType());

        List<WebhookStatusDto> statusDtoList = statusService.getAll();
        statusDtoList.sort(Comparator.comparing(WebhookStatusDto::getName));

        Page<WebhookSuccessStatusDto> resultPage = service.getPage(pageable, filter);
        model.addAttribute("data", resultPage);
        model.addAttribute("banks", Bank.values());
        model.addAttribute("statuses", statusDtoList);
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return getTemplateName();
    }

    @PostMapping
    public String create(@RequestBody WebhookSuccessStatusDto input) throws CloneNotSupportedException {
        input.setType(getWebhookType());
        service.create(input);
        return getAllPage();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }

    protected abstract WebhookType getWebhookType();

    protected abstract String getTemplateName();

    protected abstract String getAllPage();
}
