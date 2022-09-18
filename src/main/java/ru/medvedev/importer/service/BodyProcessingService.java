package ru.medvedev.importer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.InnRegionEntity;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.exception.FileProcessingException;
import ru.medvedev.importer.exception.IllegalCellTypeException;
import ru.medvedev.importer.exception.NumberProjectNotFoundException;
import ru.medvedev.importer.service.bankclientservice.VtbClientService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.utils.StringUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BodyProcessingService {

    private static final int REQUEST_BATCH_SIZE = 150;
    private static final Integer BATCH_SIZE = 100;

    private final ProjectNumberService projectNumberService;
    private final FileInfoService fileInfoService;
    private final ContactService contactService;
    private final VtbClientService vtbClientService;
    private final SkorozvonClientService skorozvonClientService;
    private final ApplicationEventPublisher eventPublisher;
    private final InnRegionService innRegionService;
    private final DownloadFilterService downloadFilterService;
    private final ObjectMapper objectMapper;

    private final Set<String> regionCodes = new HashSet<>();

    @Scheduled(cron = "${cron.tg-file-body-processor}")
    public void sendRequestToTelegram() {
        fileInfoService.getFileToProcessingBody().ifPresent(file -> {
            eventPublisher.publishEvent(new ImportEvent(this, "Взят в обработку",
                    EventType.LOG_TG, file.getId()));

            file.setProcessingStep(FileProcessingStep.READ_DATA);
            fileInfoService.save(file);
            try {
                processFile(file);
                eventPublisher.publishEvent(new ImportEvent(this, "Файл обработан",
                        EventType.SUCCESS, file.getId()));
            } catch (FileProcessingException ex) {
                log.debug("Error processing file: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.ERROR,
                        ex.getFileId()));
            } catch (Exception ex) {
                log.debug("Error processing file", ex);
                eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                        .orElse("Непредвиденная ошибка\n" + ex.getClass()), EventType.ERROR,
                        file.getId()));
            }
        });
    }

    private void processFile(FileInfoEntity file) throws IOException {

        List<List<ContactEntity>> contactBatchList = readValidContactFromFile(file);
        file.getBankList().forEach(fileBank -> {
            List<ContactEntity> resultContactList = contactBatchList.stream()
                    .flatMap(batch -> contactService.filteredContacts(batch, fileBank).stream())
                    .collect(toList());
            prepareContactToSkorozvon(resultContactList, fileBank);
        });
        contactBatchList.clear();
    }

    private List<List<ContactEntity>> readValidContactFromFile(FileInfoEntity file) throws IOException {
        FileInputStream fis = new FileInputStream(new File(file.getPath()));
        Workbook wb = file.getName().endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);
        List<String> innFilter = (List<String>) downloadFilterService.getByName(DownloadFilter.INN).getFilter();

        Sheet sheet = wb.getSheetAt(0);
        Map<XlsxRequireField, FieldPositionDto> fieldPositionMap = file.getColumnInfo().get().getFieldPositionMap();

        List<List<ContactEntity>> contactBatchList = new ArrayList<>();
        List<ContactEntity> contactBatch = new ArrayList<>();
        Map<String, InnRegionEntity> innRegionMap = innRegionService.getAllMap();
        regionCodes.clear();
        for (Row row : sheet) {
            if (file.getWithHeader() && row.getRowNum() == 0) {
                continue;
            }
            try {
                ContactEntity contact = parseContact(row, fieldPositionMap, file.getId(), innRegionMap);
                if (isBlank(contact.getInn()) ||
                        (contact.getInn().length() != 10 && contact.getInn().length() != 12) ||
                        (!innFilter.isEmpty() && innFilter.stream()
                                .anyMatch(innPrefix -> contact.getInn().startsWith(innPrefix)))) {
                    continue;
                }
                contactBatch.add(contact);
            } catch (Exception ex) {
                log.debug("*** Invalid contact rowNum = {}", row.getRowNum(), ex);
                eventPublisher.publishEvent(new ImportEvent(this,
                        String.format("Некорректная запись. Строка №%d", row.getRowNum()), EventType.LOG, file.getId()));
            }
            if (contactBatch.size() == BATCH_SIZE) {
                contactBatchList.add(contactBatch);
                contactBatch = new ArrayList<>();
            }
        }
        if (!contactBatch.isEmpty()) {
            contactBatchList.add(contactBatch);
        }
        regionCodes.forEach(code -> eventPublisher.publishEvent(new ImportEvent(this,
                String.format("Регион с кодом %s не найден в справочнике", code), EventType.LOG, file.getId())));
        wb.close();
        return contactBatchList;
    }

    private ContactEntity parseContact(Row row, Map<XlsxRequireField, FieldPositionDto> cellPositionMap, Long fileId,
                                       Map<String, InnRegionEntity> innRegionMap) {

        ContactEntity contact = new ContactEntity();
        List<TrashColumnDto> trashColumns = new ArrayList<>();
        cellPositionMap.keySet().forEach(field -> {
            FieldPositionDto positionInfo = cellPositionMap.get(field);
            if (positionInfo.getHeader().isEmpty()) {
                return;
            }

            positionInfo.getHeader().forEach(header -> {
                Cell cell = row.getCell(header.getPosition());
                switch (field) {
                    case FIO:
                        getCellValue(cell, value -> {
                            if (isNotBlank(contact.getName())) {
                                return;
                            }
                            String[] fioSplit = value.split(" ");
                            if (fioSplit.length == 3) {
                                contact.setName(fioSplit[1]);
                                contact.setSurname(fioSplit[0]);
                                contact.setName(fioSplit[2]);
                            }
                            if (fioSplit.length == 2) {
                                contact.setName(fioSplit[1]);
                                contact.setSurname(fioSplit[0]);
                            }
                        }, header, fileId);
                        break;
                    case NAME:
                        getCellValue(cell, contact::setName, header, fileId);
                        break;
                    case SURNAME:
                        getCellValue(cell, contact::setSurname, header, fileId);
                        break;
                    case MIDDLE_NAME:
                        try {
                            getCellValue(cell, contact::setMiddleName, header, fileId);
                        } catch (IllegalCellTypeException ex) {
                            log.debug("*** MiddleName is empty");
                            contact.setCity("");
                        }
                        break;
                    case ORG_NAME:
                        try {
                            getCellValue(cell, contact::setOrgName, header, fileId);
                        } catch (IllegalCellTypeException ex) {
                            log.debug("*** OrgName is empty");
                        }
                        break;
                    case PHONE:
                        getCellValue(cell, val -> contact.setPhone(new BigDecimal(addPhoneCountryCode(replaceSpecialCharacters(val)))
                                .toString()), header, fileId);

                        break;
                    case INN:
                        getCellValue(cell, val -> contact.setInn(val.length() == 9 || val.length() == 11 ? "0" + val : val), header, fileId);
                        if (isBlank(contact.getInn())) {
                            return;
                        }
                        contact.setRegion(Optional.ofNullable(innRegionMap.get(contact.getInn().substring(0, 2)))
                                .map(InnRegionEntity::getName).orElseGet(() -> {
                                    regionCodes.add(contact.getInn().substring(0, 2));
                                    return "";
                                }));
                        break;
                    case OGRN:
                        getCellValue(cell, contact::setOgrn, header, fileId);
                        break;
                    case CITY:
                        try {
                            getCellValue(cell, contact::setCity, header, fileId);  //todo бывший адрес
                        } catch (IllegalCellTypeException ex) {
                            log.debug("*** City is empty");
                            contact.setCity("");
                        }
                        break;
                    /*case TRASH:
                        TrashColumnDto columnDto = new TrashColumnDto();
                        columnDto.setColumnName(header.getValue());
                        getCellValue(cell, columnDto::setValue, header, fileId);
                        trashColumns.add(columnDto);
                        break;*/
                }
            });
        });
        if (isBlank(contact.getOrgName())) {
            contact.setOrgName(getFioStringFromContact(contact));
        }

        try {
            contact.setTrashColumns(objectMapper.writeValueAsString(trashColumns));
        } catch (Exception ex) {
            log.debug("*** Error convert trashColumn to String");
        }
        return contact;
    }

    private void getCellValue(Cell cell, Consumer<String> contactFieldSetter, HeaderDto header, long fileId) {
        if (cell == null) {
            contactFieldSetter.accept("");
            return;
        }
        switch (cell.getCellType()) {
            case STRING:
                contactFieldSetter.accept(Optional.ofNullable(cell.getStringCellValue()).orElse(""));
                break;
            case NUMERIC:
                contactFieldSetter.accept(Optional.of(cell.getNumericCellValue())
                        .map(val -> new BigDecimal(val).toString()).orElse(""));
                break;
            default:
                throw new IllegalCellTypeException(String.format("Неверный тип поля Строка [%d], " +
                        "Столбец [%s]", cell.getRowIndex() + 1, header.getPosition() + 1), fileId);
        }
    }

    private void prepareContactToSkorozvon(List<ContactEntity> contacts, FileInfoBankEntity fileBank) {
        List<LeadInfoResponse> positiveLead = new ArrayList<>();
        List<LeadInfoResponse> negativeLead = new ArrayList<>();

        for (int i = 0; i < contacts.size(); i = i + REQUEST_BATCH_SIZE) {
            List<ContactEntity> contactSublist = contacts.subList(i, Math.min(i + REQUEST_BATCH_SIZE, contacts.size()));
            vtbClientService.getAllFromCheckLead(contactSublist.stream()
                    .map(ContactEntity::getInn)
                    .collect(toList()), fileBank.getFileInfoId()).forEach(lead -> {
                if (lead.getResponseCode() == CheckLeadStatus.POSITIVE) {
                    positiveLead.add(lead);
                } else {
                    negativeLead.add(lead);
                }
            });
        }
        contactService.changeContactStatus(negativeLead, fileBank.getFileInfoId(), ContactStatus.REJECTED);
        sendContactToSkorozvon(contacts, positiveLead, fileBank);
    }

    private void sendContactToSkorozvon(List<ContactEntity> contacts, List<LeadInfoResponse> leads,
                                        FileInfoBankEntity fileBank) {

        FileInfoEntity fileInfo = fileBank.getFileInfo();
        if (fileBank.getProjectId() == null) {
            throw new NumberProjectNotFoundException("Не указан номер проекта", fileInfo.getId());
        }

        List<CreateOrganizationDto> orgList = contacts.stream()
                .filter(contact -> leads.stream().anyMatch(lead -> lead.getInn().equals(contact.getInn())))
                .collect(groupingBy(ContactEntity::getInn)).values()
                .stream()
                .map(contactList -> {
                    CreateOrganizationDto orgDto = xlsxRecordToOrganization(contactList.get(0));
                    contactList.forEach(contact -> orgDto.getLeads().add(xlsxRecordToLead(contact)));
                    return orgDto;
                }).collect(toList());

        for (int i = 0; i < orgList.size(); i = i + REQUEST_BATCH_SIZE) {
            skorozvonClientService.createMultiple(fileBank.getProjectId(),
                    orgList.subList(i, Math.min(i + REQUEST_BATCH_SIZE, orgList.size())),
                    Collections.singletonList(fileInfo.getName()));
        }
        contactService.changeContactStatus(leads, fileInfo.getId(), ContactStatus.DOWNLOADED);
    }

    private static CreateLeadDto xlsxRecordToLead(ContactEntity contact) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(getFioStringFromContact(contact));
        lead.setPhones(Collections.singletonList(contact.getPhone()));
        lead.setCity(contact.getCity());
        lead.setRegion(contact.getRegion());
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(ContactEntity contact) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        organization.setName(String.format("%s %s", getFioStringFromContact(contact), contact.getOrgName()));
        organization.setPhones(Collections.singletonList(contact.getPhone()));
        organization.setHomepage(String.format("https://api.whatsapp.com/send?phone=%s", contact.getPhone()));
        organization.setCity(contact.getCity());
        organization.setRegion(contact.getRegion());
        organization.setInn(contact.getInn());
        organization.setComment(contact.getOgrn());
        return organization;
    }
}
