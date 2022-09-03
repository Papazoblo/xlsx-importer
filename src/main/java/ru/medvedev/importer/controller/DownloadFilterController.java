package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.request.DownloadFilterRequest;
import ru.medvedev.importer.enums.DownloadFilter;
import ru.medvedev.importer.service.DownloadFilterService;

import java.util.Arrays;
import java.util.Comparator;

import static java.util.stream.Collectors.toList;
import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;


@Controller
@RequiredArgsConstructor
@RequestMapping("/settings/filters")
public class DownloadFilterController {

    private final DownloadFilterService service;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(required = false, defaultValue = "INN") DownloadFilter filter) {

        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("currentFilter", filter);
        model.addAttribute("filterList", Arrays.stream(DownloadFilter.values())
                .sorted(Comparator.comparing(DownloadFilter::getDescription)).collect(toList()));
        model.addAttribute("filter", service.getByName(filter));
        return "download_filter";
    }

    @PostMapping
    public ResponseEntity<Boolean> saveFilter(@RequestBody DownloadFilterRequest request) {
        service.saveFilter(request);
        return ResponseEntity.ok(true);
    }
}
