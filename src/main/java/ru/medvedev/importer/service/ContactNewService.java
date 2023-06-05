package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.entity.*;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.repository.ContactBankActualityRepository;
import ru.medvedev.importer.repository.ContactNewRepository;
import ru.medvedev.importer.service.export.exporter.ContactReloadExporterService;
import ru.medvedev.importer.service.sheethandler.ContactNewSheetHandler;
import ru.medvedev.importer.specification.ContactSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static feign.Util.isNotBlank;
import static java.util.stream.Collectors.toMap;
import static ru.medvedev.importer.enums.ContactActuality.PROCESSED;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactNewService {

    private static final Long DEFAULT_ERROR_WEBHOOK_ID = -2L;

    private final WebhookSuccessStatusService successStatusService;
    private final ContactNewRepository repository;
    private final SystemVariableService systemVariableService;
    private final ContactBankActualityRepository contactBankActualityRepository;
    private final ContactReloadExporterService contactReloadExporterService;

    public Page<ContactDto> getPage(ContactFilter filter, Pageable pageable) {
        Page<ContactNewEntity> page = repository.findAll(ContactSpecification.of(filter), pageable);
        return new PageImpl<>(page.getContent().stream()
                .map(ContactDto::of).collect(Collectors.toList()),
                page.getPageable(), page.getTotalElements());
    }

    public Page<ContactCountDateProjection> getContactCountDateStatistic(ContactCountDateStatisticFilter filter) {
        return new PageImpl<>(repository.getCountDateStatistic(Optional.ofNullable(filter.getDateFrom()).map(LocalDate::toString).orElse(null),
                Optional.ofNullable(filter.getDateTo()).map(LocalDate::toString).orElse(null), (filter.getOffset() - 1) * filter.getLimit(), filter.getLimit()),
                PageRequest.of(filter.getOffset(), filter.getLimit()),
                repository.getTotalRecordCountDateStatistic());
    }

    public void updateActuality(String inn, Bank bank, WebhookStatusEntity newWebhook) {

        repository.findFirstByInn(inn).ifPresent(contact -> {

            ContactBankActualityEntity actualityEntity = contact.getActualityList().stream()
                    .filter(actuality -> actuality.getBank() == bank)
                    .findFirst().orElseGet(() -> {
                        ContactBankActualityEntity actuality = new ContactBankActualityEntity();
                        actuality.setBank(bank);
                        actuality.setContactId(contact.getId());
                        return actuality;
                    });

            List<WebhookSuccessStatusEntity> webhookList = successStatusService.getByBankAndWebhook(bank, newWebhook.getId());
            Integer maxErrorNumber = systemVariableService.getByCode(SystemVariable.MAX_ERROR_NUMBER)
                    .map(item -> isNotBlank(item.getValue()) ? Integer.parseInt(item.getValue()) : 3)
                    .orElse(3);

            if (webhookList.stream().anyMatch(webhook -> webhook.getType() == WebhookType.ERROR)) {
                actualityEntity.incErrorCount();
                if (actualityEntity.getErrorCount() > maxErrorNumber) {
                    actualityEntity.setActuality(PROCESSED);
                }
                actualityEntity.setWebhookStatusId(DEFAULT_ERROR_WEBHOOK_ID);
                contactBankActualityRepository.save(actualityEntity);
            } else if (webhookList.stream().anyMatch(webhook -> webhook.getType() == WebhookType.SUCCESS)) {
                actualityEntity.setActuality(PROCESSED);
                actualityEntity.setWebhookStatusId(newWebhook.getId());
                contactBankActualityRepository.save(actualityEntity);
            }
        });
    }

    public boolean existsByInn(String inn) {
        return repository.existsByInn(inn);
    }

    public Optional<ContactNewEntity> getByInn(String inn) {
        return repository.findFirstByInn(inn);
    }

    public List<ContactNewEntity> getContactIdByInn(List<String> innList) {
        return repository.findAllContactByInn(innList);
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

    public static void readDataFromSheet(XMLReader parser, XSSFReader reader) throws IOException, SAXException, InvalidFormatException {
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
