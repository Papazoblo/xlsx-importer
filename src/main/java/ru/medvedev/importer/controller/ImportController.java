package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.dto.AutoLinkXlsxFieldDto;
import ru.medvedev.importer.dto.ContactDto;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.dto.XlsxImportInfo;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.DownloadAction;
import static ru.medvedev.importer.enums.DownloadAction.ACTION_LEAD_BANK_CHECK;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.service.AutoLinkXlsxFieldService;
import ru.medvedev.importer.service.FileInfoService;
import ru.medvedev.importer.service.LeadWorkerService;
import ru.medvedev.importer.service.XlsxParserService;
import ru.medvedev.importer.service.XlsxStorageService;
import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @GetMapping("/xlsx/import/{downloadAction}")
    public String importXlsx(Model model,
                             @PathVariable("downloadAction") DownloadAction downloadAction) {
        try {
            Optional<FileInfoEntity> fileInfoOptional = fileInfoService.getNewUiFileToInitialize(downloadAction);
            if (fileInfoOptional.isPresent() && fileInfoOptional.get().getAction() == downloadAction) {
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
        model.addAttribute("fields", downloadAction == ACTION_LEAD_BANK_CHECK
                ? Arrays.asList(SkorozvonField.ORG_INN, SkorozvonField.ORG_PHONE)
                : SkorozvonField.values());

        if (downloadAction != DownloadAction.ACTION_ALFA_SYSTEM_DOWNLOAD
                && downloadAction != ACTION_LEAD_BANK_CHECK) {
            List<List<Bank>> banks = new ArrayList<>();
            Bank[] bankArray = Bank.valuesNotExclude();
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
        }
        return downloadAction.getTemplate();
    }

    @PostMapping("/xlsx/import/{downloadAction}")
    @ResponseBody
    public void startImport(@PathVariable("downloadAction") DownloadAction downloadAction,
                            @RequestBody XlsxImportInfo importInfo) {

        if (downloadAction == ACTION_LEAD_BANK_CHECK) {
            importInfo.getBanksProject().put(Bank.ALFA, 777L);
        }

        fileInfoService.addUiColumnInfo(downloadAction, importInfo);
    }

    @PostMapping(value = "/xlsx/upload/{downloadAction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> upload(@PathVariable("downloadAction") DownloadAction downloadAction,
                                         @RequestParam("file") MultipartFile file) {
        try {
            if (downloadAction == DownloadAction.ACTION_WEBHOOK_DOWNLOAD) {
                leadWorkerService.massLoadWebhooks(file);
            } else {
                xlsxStorageService.upload(downloadAction, file);
            }
            return ResponseEntity.ok(file.getOriginalFilename());
        } catch (IOException io) {
            throw new BadRequestException("Невозможно загрузить файл");
        }
    }

    @PostMapping("/skorozvon/webhook")
    @ResponseBody
    public void takeWebhook(@RequestBody WebhookDto input) {
        log.debug("*** Take a webhook {}", input);
        //leadWorkerService.processWebhook(new ObjectMapper().readValue(input, WebhookDto.class));
        leadWorkerService.processWebhook(input);
    }

    @PostMapping("/skorozvon/webhook/with-create-contact")
    @ResponseBody
    public ResponseEntity<Integer> createWithFixWebhook(@RequestBody List<ContactDto> contactList,
                                                        @RequestHeader("key") String key) {
        LocalDate date = LocalDate.now();

        if (!key.equals(date.format(DateTimeFormatter.ofPattern("dd:MM")))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(leadWorkerService.processWithCreateContact(contactList));
    }

    @PostMapping(value = "/skorozvon/webhook/test", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void takeWebhook(@RequestParam("text") String text) {
        leadWorkerService.sendTest(text);
    }
}
