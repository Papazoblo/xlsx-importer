package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.medvedev.importer.dto.EventDto;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.service.EventService;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createAt")));

        Page<EventDto> resultPage = service.getPage(pageable);
        model.addAttribute("events", resultPage);
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "events";
    }
}
