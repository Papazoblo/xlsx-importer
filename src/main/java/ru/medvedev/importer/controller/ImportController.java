package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.service.SkorozvonClientService;
import ru.medvedev.importer.service.XlsxParserService;
import ru.medvedev.importer.service.XlsxStorageService;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Log4j2
public class ImportController {

    private final XlsxParserService xlsxParserService;
    private final XlsxStorageService xlsxStorageService;
    private final SkorozvonClientService clientService;

    @GetMapping
    public String importXlsx(Model model) {
        model.addAttribute("test", "Test");
        try {
            //clientService.login();
            model.addAttribute("headers", xlsxParserService.readColumnHeaders());
        } catch (Exception ex) {
            log.info("Error read xlsx", ex);
        }
        return "main";
    }

    @PostMapping("/xlsx/upload")
    public void upload(@RequestParam("file") MultipartFile file) {
        try {
            xlsxStorageService.upload(file);
        } catch (IOException io) {
            throw new BadRequestException("Невозможно загрузить файл");
        }
    }
}
