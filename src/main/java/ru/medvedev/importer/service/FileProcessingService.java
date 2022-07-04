package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.CreateLeadDto;
import ru.medvedev.importer.dto.CreateOrganizationDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.InnRegionEntity;
import ru.medvedev.importer.enums.CheckLeadStatus;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.exception.BadRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private static final int REQUEST_BATCH_SIZE = 150;
    private static final Integer BATCH_SIZE = 100;

    private final ProjectNumberService projectNumberService;
    private final FileInfoService fileInfoService;
    private final FieldNameVariantService fieldNameVariantService;
    private final ContactService contactService;
    private final VtbClientService vtbClientService;
    private final SkorozvonClientService skorozvonClientService;
    private final ApplicationEventPublisher eventPublisher;
    private final InnRegionService innRegionService;

    @Scheduled(cron = "${cron.launch-file-processing}")
    public void launchProcessFile() {
        if (fileInfoService.isExistsInProcess()) {
            return;
        }

        fileInfoService.getDownloadedFile().ifPresent(entity -> {
            log.debug("*** launch file processing [{}, id = {}]", entity.getName(), entity.getId());
            entity = fileInfoService.changeStatus(entity, FileStatus.IN_PROCESS);
            processFile(entity);
        });
    }

    private void processFile(FileInfoEntity entity) {
        Map<XlsxRequireField, List<String>> namesMap = fieldNameVariantService.getAll();
        if (namesMap.keySet().stream().anyMatch(key -> namesMap.get(key).isEmpty())) {
            eventPublisher.publishEvent(new ImportEvent(this, "Не установлены варианты названий полей",
                    EventType.ERROR, entity.getId()));
            fileInfoService.changeStatus(entity, FileStatus.ERROR);
            throw new BadRequestException("asd");
        }

        readFile(entity, namesMap);
        new File(entity.getPath()).delete();
    }

    private void readFile(FileInputStream fis, Map<XlsxRequireField, List<String>> namesMap, Long fileId)
            throws IOException {

        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);
        Map<XlsxRequireField, Integer> fieldPositionMap = new HashMap<>();

        List<ContactEntity> contactBatch = new ArrayList<>();
        List<ContactEntity> resultContactList = new ArrayList<>();
        Map<String, InnRegionEntity> innRegionMap = innRegionService.getAllMap();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                try {
                    fieldPositionMap = readHeader(row, namesMap, fileId);
                } catch (BadRequestException ex) {
                    return;
                }
            } else {
                contactBatch.add(parseContact(row, fieldPositionMap, fileId, innRegionMap));
            }
            if (contactBatch.size() == BATCH_SIZE) {
                resultContactList.addAll(contactService.filteredContacts(contactBatch, fileId));
                contactBatch.clear();
            }
        }
        if (!contactBatch.isEmpty()) {
            resultContactList.addAll(contactService.filteredContacts(contactBatch, fileId));
            contactBatch.clear();
        }
        wb.close();
        prepareContactToSkorozvon(resultContactList, fileId);
    }

    private void readFile(FileInfoEntity entity, Map<XlsxRequireField, List<String>> namesMap) {
        try {
            FileInputStream fis = new FileInputStream(new File(entity.getPath()));
            readFile(fis, namesMap, entity.getId());
            fis.close();
            fileInfoService.changeStatus(entity, FileStatus.SUCCESS);
        } catch (Exception ex) {
            eventPublisher.publishEvent(new ImportEvent(this, "Невозможно открыть файл", EventType.ERROR,
                    entity.getId()));
            fileInfoService.changeStatus(entity, FileStatus.ERROR);
        }
    }

    private Map<XlsxRequireField, Integer> readHeader(Row row, Map<XlsxRequireField, List<String>> namesMap, Long fileId) {
        Map<XlsxRequireField, Integer> positionField = new HashMap<>();
        namesMap.keySet().forEach(key -> {
            for (Cell cell : row) {

                if (cell.getCellType() != CellType.STRING) {
                    eventPublisher.publishEvent(new ImportEvent(this, "В файле отсутствует шапка таблицы",
                            EventType.ERROR, fileId));
                    return;
                }

                if (namesMap.get(key).stream()
                        .anyMatch(name -> name.toLowerCase().equals(cell.getStringCellValue()))) {
                    positionField.put(key, cell.getColumnIndex());
                    break;
                }
            }

            if (!positionField.containsKey(key)) {
                eventPublisher.publishEvent(new ImportEvent(this, String.format("Столбец %s не найден в файле",
                        key.getDescription()), EventType.ERROR, fileId));
                throw new BadRequestException("Column not found");
            }
        });
        return positionField;
    }

    private ContactEntity parseContact(Row row, Map<XlsxRequireField, Integer> cellPositionMap, Long fileId,
                                       Map<String, InnRegionEntity> innRegionMap) {

        ContactEntity contact = new ContactEntity();
        cellPositionMap.keySet().forEach(field -> {
            Integer position = cellPositionMap.get(field);
            Cell cell = row.getCell(position);
            switch (field) {
                case NAME:
                    getCellValue(cell, contact::setName, position, fileId);
                    break;
                case SURNAME:
                    getCellValue(cell, contact::setSurname, position, fileId);
                    break;
                case MIDDLE_NAME:
                    getCellValue(cell, contact::setMiddleName, position, fileId);
                    break;
                case ORG_NAME:
                    getCellValue(cell, contact::setOrgName, position, fileId);
                    break;
                case PHONE:
                    getCellValue(cell, val -> contact.setPhone(replaceSpecialCharacters(val)), position, fileId);
                    break;
                case INN:
                    getCellValue(cell, contact::setInn, position, fileId);
                    contact.setRegion(Optional.ofNullable(innRegionMap.get(contact.getInn().substring(0, 2)))
                            .map(InnRegionEntity::getName).orElseGet(() -> {
                                eventPublisher.publishEvent(new ImportEvent(this, String.format("В справочнике отсутствует " +
                                        "регион с кодом '%s'", contact.getInn().substring(0, 2)), EventType.NOTIFICATION,
                                        fileId));
                                return "";
                            }));
                    break;
                case OGRN:
                    getCellValue(cell, contact::setOgrn, position, fileId);
                    break;
                case ADDRESS:
                    getCellValue(cell, contact::setAddress, position, fileId);
                    break;
            }
        });
        return contact;
    }

    private void getCellValue(Cell cell, Consumer<String> contactFieldSetter, int cellIndex, long fileId) {
        switch (cell.getCellType()) {
            case STRING:
                contactFieldSetter.accept(Optional.ofNullable(cell.getStringCellValue()).orElse(""));
                break;
            case NUMERIC:
                contactFieldSetter.accept(Optional.ofNullable(cell.getStringCellValue())
                        .map(String::valueOf).orElse(""));
                break;
            default:
                eventPublisher.publishEvent(new ImportEvent(this, String.format("Неверный тип поля Строка [%d], " +
                        "Столбец [%s]", cell.getRowIndex() + 1, cellIndex + 1), EventType.NOTIFICATION, fileId));
        }
    }

    private void prepareContactToSkorozvon(List<ContactEntity> contacts, Long fileId) {
        vtbClientService.login();
        List<LeadInfoResponse> positiveLead = new ArrayList<>();
        List<LeadInfoResponse> negativeLead = new ArrayList<>();
        vtbClientService.getAllFromCheckLead(contacts.stream()
                .map(ContactEntity::getInn)
                .collect(Collectors.toList())).forEach(lead -> {
            if (lead.getResponseCode() == CheckLeadStatus.POSITIVE) {
                positiveLead.add(lead);
            } else {
                negativeLead.add(lead);
            }
        });
        contactService.rejectingContact(negativeLead, fileId);
        sendContactToSkorozvon(contacts, positiveLead, fileId);
    }

    private void sendContactToSkorozvon(List<ContactEntity> contacts, List<LeadInfoResponse> leads, Long fileId) {

        String fileName = fileInfoService.getById(fileId).getName();
        Long projectNumber = projectNumberService.getNumberByDate(LocalDate.now());
        if (projectNumber == null) {
            eventPublisher.publishEvent(new ImportEvent(this, String.format("Для даты %s не указан номер проекта",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))), EventType.ERROR, fileId));
            return;
        }

        List<CreateOrganizationDto> orgList = contacts.stream()
                .filter(contact -> leads.stream().anyMatch(lead -> lead.getInn().equals(contact.getInn())))
                .collect(groupingBy(ContactEntity::getInn)).values()
                .stream()
                .map(contactList -> {
                    CreateOrganizationDto orgDto = xlsxRecordToOrganization(contactList.get(0));
                    contactList.forEach(contact -> orgDto.getLeads().add(xlsxRecordToLead(contact)));
                    return orgDto;
                }).collect(Collectors.toList());

        for (int i = 0; i < orgList.size(); i = i + REQUEST_BATCH_SIZE) {
            skorozvonClientService.createMultiple(projectNumber,
                    orgList.subList(i, Math.min(i + REQUEST_BATCH_SIZE, orgList.size())),
                    Collections.singletonList(fileName));
        }
        eventPublisher.publishEvent(new ImportEvent(this, String.format("Количество импортированных контактов %d",
                orgList.size()), EventType.NOTIFICATION, fileId));
    }

    private static CreateLeadDto xlsxRecordToLead(ContactEntity contact) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(String.format("%s %s %s", contact.getSurname(), contact.getName(), contact.getMiddleName()));
        lead.setPhones(Collections.singletonList(contact.getPhone()));
        lead.setAddress(contact.getAddress());
        lead.setRegion(contact.getRegion());
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(ContactEntity contact) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        organization.setName(contact.getOrgName());
        organization.setPhones(Collections.singletonList(contact.getPhone()));
        organization.setHomepage(String.format("https://api.whatsapp.com/send?phone=%s", contact.getPhone()));
        organization.setAddress(contact.getAddress());
        organization.setRegion(contact.getRegion());
        organization.setInn(contact.getInn());
        organization.setComment(contact.getOgrn());
        return organization;
    }

    private static String replaceSpecialCharacters(String val) {
        return val.replaceAll("[+*_()#\\-\"'$№%^&?]+", val);
    }
}
