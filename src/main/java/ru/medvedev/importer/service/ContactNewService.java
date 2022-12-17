package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import ru.medvedev.importer.dto.ContactDto;
import ru.medvedev.importer.dto.ContactFilter;
import ru.medvedev.importer.dto.ContactReloadDto;
import ru.medvedev.importer.dto.ContactStatistic;
import ru.medvedev.importer.entity.*;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.ContactStatus;
import ru.medvedev.importer.enums.ExportType;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.repository.ContactBankActualityRepository;
import ru.medvedev.importer.repository.ContactNewRepository;
import ru.medvedev.importer.service.export.exporter.ContactReloadExporterService;
import ru.medvedev.importer.service.sheethandler.ContactNewSheetHandler;
import ru.medvedev.importer.specification.ContactSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static ru.medvedev.importer.enums.ContactActuality.NEW;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactNewService {

    private static final Long DEFAULT_ERROR_WEBHOOK_ID = -2L;

    private final WebhookSuccessStatusService successStatusService;
    private final ContactNewRepository repository;
    private final WebhookStatusMapService webhookStatusMapService;
    private final ContactBankActualityRepository contactBankActualityRepository;
    private final ContactReloadExporterService contactReloadExporterService;

    public Page<ContactDto> getPage(ContactFilter filter, Pageable pageable) {
        Page<ContactNewEntity> page = repository.findAll(ContactSpecification.of(filter), pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(ContactDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public void updateActuality(String inn, Bank bank, WebhookStatusEntity newWebhook) {

        Map<Long, List<WebhookStatusMapEntity>> map = webhookStatusMapService.getMap(bank);

        repository.findFirstByInn(inn).ifPresent(contact -> {

            ContactBankActualityEntity actualityEntity = contact.getActualityList().stream()
                    .filter(actuality -> actuality.getBank() == bank)
                    .findFirst().orElseGet(() -> {
                        ContactBankActualityEntity actuality = new ContactBankActualityEntity();
                        actuality.setBank(bank);
                        actuality.setContactId(contact.getId());
                        return actuality;
                    });

            //todo webhook_id is null exception
            Optional.ofNullable(updateActuality(newWebhook, map, actualityEntity))
                    .ifPresent(contactBankActualityRepository::save);
        });
    }

    private ContactBankActualityEntity updateActuality(WebhookStatusEntity newWebhook,
                                                       Map<Long, List<WebhookStatusMapEntity>> map,
                                                       ContactBankActualityEntity actuality) {

        List<WebhookSuccessStatusEntity> webhookList = successStatusService.getByWebhook(newWebhook.getId());

        if (webhookList.stream().anyMatch(item -> item.getType() == WebhookType.ERROR)) {

            Map<Integer, WebhookStatusMapEntity> statusMap = map.get(DEFAULT_ERROR_WEBHOOK_ID).stream()
                    .collect(toMap(WebhookStatusMapEntity::getErrorCount, item -> item));

            Integer errorCount = actuality.incErrorCount();
            if (actuality.getActuality() == NEW) {
                actuality.setActuality(Optional.ofNullable(statusMap.get(errorCount))
                        .map(WebhookStatusMapEntity::getActuality)
                        .orElseGet(() -> statusMap.keySet().stream().max(Integer::compareTo)
                                .map(maxItem -> statusMap.get(maxItem).getActuality())
                                .orElse(NEW)));
                actuality.setWebhookStatusId(DEFAULT_ERROR_WEBHOOK_ID);
            }
        } else if (webhookList.stream().anyMatch(item -> item.getType() == WebhookType.SUCCESS)) {

            WebhookStatusMapEntity newStatusMap = Optional.ofNullable(map.get(newWebhook.getId()))
                    .map(item -> item.get(0))
                    .orElse(null);
            WebhookStatusMapEntity oldStatusMap = Optional.ofNullable(map.get(actuality.getWebhookStatusId()))
                    .map(item -> item.get(0))
                    .orElse(null);

            if (newStatusMap != null) {
                if (actuality.getActuality() == NEW || oldStatusMap == null
                        || Optional.ofNullable(oldStatusMap.getPriority()).orElse(0) < Optional.ofNullable(newStatusMap.getPriority()).orElse(0)) {
                    actuality.setActuality(newStatusMap.getActuality());
                    actuality.setWebhookStatusId(newWebhook.getId());
                }
            } else {
                return null;
            }
        } else { // == MINOR
            // ничего не делаем
        }
        return actuality;
    }

    public boolean existsByInn(String inn) {
        return repository.existsByInn(inn);
    }

    public Optional<ContactNewEntity> getByInn(String inn) {
        return repository.findFirstByInn(inn);
    }

    public List<Long> getContactIdByInn(List<String> innList) {
        return repository.findContactIdByInn(innList);
    }

    public void save(List<ContactNewEntity> entities) {
        repository.saveAll(entities);
    }

    public Set<String> importContacts(MultipartFile file) throws IOException {


        try (InputStream inputStream = file.getInputStream()) {

            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
            XMLReader parser = XMLHelper.newXMLReader();
            ContactNewSheetHandler handler = new ContactNewSheetHandler(sst, repository);
            parser.setContentHandler(handler);
            readDataFromSheet(parser, r);
            repository.saveAll(handler.getContacts());
            return handler.getInnList();
        } catch (Exception e) {
            log.error("Cannot read excel-file '{}'", file.getName(), e);
            return Collections.emptySet();
        }
    }

    private void readDataFromSheet(XMLReader parser, XSSFReader reader) throws IOException, SAXException, InvalidFormatException {
        Iterator<InputStream> sheets = reader.getSheetsData();
        int i = 0;
        while (sheets.hasNext()) {
            if (i > 0) {
                return;
            }
            System.out.println("Start process sheet");
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
            System.out.println("End process sheet");
            i++;
        }
    }

    private static boolean isFileNotValid(MultipartFile file) {
        if (file == null) {
            log.error("Excel file is null");
            return true;
        }
        return isFileExtensionNotValid(file.getOriginalFilename());
    }

    private static boolean isFileExtensionNotValid(String fileName) {
        String fileExtension = FilenameUtils.getExtension(fileName);

        if (fileExtension == null || fileExtension.isEmpty()
                || (!fileExtension.equalsIgnoreCase("xls") && !fileExtension.equalsIgnoreCase("xlsx"))) {
            log.error("Bad file extension for Excel file.");
            return true;
        }
        return false;
    }

    public InputStreamResource export(ContactFilter filter) {
        List<ContactNewEntity> contacts = repository.findAll(ContactSpecification.of(filter));
        return contactReloadExporterService.exporting(ExportType.XLSX, contacts.stream()
                .map(ContactReloadDto::of)
                .collect(Collectors.toList()));
    }

    public Map<ContactStatus, Long> getContactStatisticByFileId(Long fileId) {
        return repository.getContactStatisticByFileId(fileId).stream()
                .collect(toMap(ContactStatistic::getStatus, ContactStatistic::getCount));
    }
}
