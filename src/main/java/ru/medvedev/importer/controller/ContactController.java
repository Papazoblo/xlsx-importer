package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.medvedev.importer.dto.ContactDto;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.service.ContactService;
import ru.medvedev.importer.service.export.exporter.ContactExporterService;

import java.io.IOException;
import java.time.LocalDate;

import static ru.medvedev.importer.enums.ExportType.XLSX;
import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService service;
    private final ContactExporterService exporterService;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "15") int size,
                          ContactFilter contactFilter) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createAt")));

        Page<ContactDto> resultPage = service.getPage(contactFilter, pageable);
        model.addAttribute("contacts", resultPage);
        model.addAttribute("filter", contactFilter);
        model.addAttribute("banks", Bank.values());
        model.addAttribute("statuses", ContactStatus.values());
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "contacts";
    }

    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(ContactFilter filter) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contact_export_" + LocalDate.now().toString() + ".xlsx\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exporterService.exporting(XLSX, service.findAll(filter)));
    }
}
