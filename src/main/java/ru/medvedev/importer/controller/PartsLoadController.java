package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.service.PartsLoadService;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/parts-load")
public class PartsLoadController {

    private final PartsLoadService service;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createDate")));

        Page<PartsLoadDto> resultPage = service.getPage(pageable);
        model.addAttribute("loads", resultPage);
        model.addAttribute("banks", Bank.valuesNotExclude());
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "parts_load";
    }

    @PostMapping
    @ResponseBody
    public void importContact(@RequestBody PartsLoadDto input) {
        service.create(input);
    }
}
