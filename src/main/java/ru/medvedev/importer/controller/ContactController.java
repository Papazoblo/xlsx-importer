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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.dto.ContactDto;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactActuality;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.ContactNewService;
import ru.medvedev.importer.service.FileProcessingService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;
import ru.medvedev.importer.service.export.exporter.ContactExporterService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactNewService service;
    private final FileProcessingService fileProcessingService;
    private final WebhookSuccessStatusService statusService;
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
        model.addAttribute("actualities", ContactActuality.values());
        model.addAttribute("webhooks", statusService.findWebhookStatusByType(Collections.singletonList(WebhookType.SUCCESS)));
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "contacts";
    }

    @GetMapping(value = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    public void importContact(@RequestPart MultipartFile file,
                              @RequestPart XlsxImportInfo info) throws IOException {

        fileProcessingService.importContacts(file, info);
    }

    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Resource> export(ContactFilter filter) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contact_new_export_" + LocalDate.now().toString() + ".xlsx\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(service.export(filter));
    }

    /*@GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(ContactFilter filter) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contact_export_" + LocalDate.now().toString() + ".xlsx\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(exporterService.exporting(XLSX, service.findAll(filter)));
    }*/
}
