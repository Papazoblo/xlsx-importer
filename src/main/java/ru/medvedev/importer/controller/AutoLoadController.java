package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.medvedev.importer.service.ContactService;
import ru.medvedev.importer.service.export.exporter.ContactExporterService;

@Controller
@RequestMapping("/auto-load")
@RequiredArgsConstructor
public class AutoLoadController {

    private final ContactService service;
    private final ContactExporterService exporterService;

    /*@GetMapping
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
    }*/
}
