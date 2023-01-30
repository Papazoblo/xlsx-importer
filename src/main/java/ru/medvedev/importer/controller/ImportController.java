package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.dto.AutoLinkXlsxFieldDto;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.service.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class ImportController {

    private final XlsxParserService xlsxParserService;
    private final XlsxStorageService xlsxStorageService;
    private final LeadWorkerService leadWorkerService;
    private final FileInfoService fileInfoService;
    private final AutoLinkXlsxFieldService autoLinkXlsxFieldService;

    @GetMapping("/xlsx/import")
    public String importXlsx(Model model) {
        try {
            Optional<FileInfoEntity> fileInfoOptional = fileInfoService.getNewUiFileToInitialize();
            if (fileInfoOptional.isPresent()) {
                FileInfoEntity fileInfo = fileInfoOptional.get();
                File file = new File(fileInfo.getPath());

                List<String> headers = xlsxParserService.readColumnHeaders(fileInfo, file);
                Map<SkorozvonField, AutoLinkXlsxFieldDto> fieldNameVariantMap = autoLinkXlsxFieldService.getAll();
                Map<SkorozvonField, Set<Integer>> mapSelectedField = new HashMap<>();
                Arrays.stream(SkorozvonField.values()).forEach(skorozvonField -> {
                    if (fieldNameVariantMap.containsKey(skorozvonField)) {
                        mapSelectedField.put(skorozvonField, fieldNameVariantMap.get(skorozvonField).getColumns());
                    } else {
                        mapSelectedField.put(skorozvonField, Collections.emptySet());
                    }
                });

                model.addAttribute("callProjectId", "");
                model.addAttribute("fileExist", file.exists());
                model.addAttribute("fileName", fileInfo.getName());
                model.addAttribute("fileId", fileInfo.getId());
                model.addAttribute("headers", file.exists() ? headers : Collections.emptyList());
                model.addAttribute("fieldNameGroup", mapSelectedField);
            } else {
                model.addAttribute("callProjectId", "");
                model.addAttribute("fileExist", false);
                model.addAttribute("fileName", "Не найден");
                model.addAttribute("fileId", -1L);
                model.addAttribute("headers", Collections.emptyList());
                model.addAttribute("fieldNameGroup", Collections.emptyMap());
            }
        } catch (Exception ex) {
            log.info("Error read xlsx", ex);
            model.addAttribute("callProjectId", "");
            model.addAttribute("fileExist", false);
            model.addAttribute("fileName", "Не найден");
            model.addAttribute("fileId", -1L);
            model.addAttribute("headers", Collections.emptyList());
        }
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("fields", SkorozvonField.values());

        List<List<Bank>> banks = new ArrayList<>();
        Bank[] bankArray = Bank.values();
        for (int i = 0; i < bankArray.length; i += 3) {
            List<Bank> bankInner = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                if (i + j < bankArray.length) {
                    bankInner.add(bankArray[i + j]);
                }
            }
            banks.add(bankInner);
        }
        model.addAttribute("banks", banks);
        return "main";
    }

    @PostMapping("/xlsx/import")
    @ResponseBody
    public void startImport(@RequestBody XlsxImportInfo importInfo) {
        fileInfoService.addUiColumnInfo(importInfo);
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
        log.debug("*** Take a webhook {}", input);
        leadWorkerService.processWebhook(input);
    }
}
