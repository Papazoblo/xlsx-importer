package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.component.XlsxStorage;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.service.LeadWorkerService;
import ru.medvedev.importer.service.XlsxParserService;
import ru.medvedev.importer.service.XlsxStorageService;

import java.io.IOException;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Log4j2
public class ImportController {

    private final XlsxParserService xlsxParserService;
    private final XlsxStorageService xlsxStorageService;
    private final XlsxStorage storage;
    private final LeadWorkerService leadWorkerService;

    @GetMapping("/xlsx/import")
    public String importXlsx(Model model) {
        try {
            model.addAttribute("fileExist", storage.isExist());
            model.addAttribute("fileName", storage.isExist() ? storage.getFileName() : "Не найден");
            model.addAttribute("headers", storage.isExist() ? xlsxParserService.readColumnHeaders()
                    : Collections.emptyList());
            model.addAttribute("fields", SkorozvonField.values());
        } catch (Exception ex) {
            log.info("Error read xlsx", ex);
        }
        return "main";
    }

    @PostMapping("/xlsx/import")
    @ResponseBody
    public void startImport(@RequestBody XlsxImportInfo importInfo) {
        leadWorkerService.processXlsxRecords(importInfo);
    }

    @PostMapping(value = "/xlsx/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            xlsxStorageService.upload(file);
            return ResponseEntity.ok(file.getOriginalFilename());
        } catch (IOException io) {
            throw new BadRequestException("Невозможно загрузить файл");
        }
    }

    @PostMapping("/webhook")
    @ResponseBody
    public void takeWebhook(@RequestBody WebhookDto input) {
        leadWorkerService.processWebhook(input);
    }

    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }
}
