package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.component.XlsxStorage;
import ru.medvedev.importer.dto.ColumnInfoDto;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.FileProcessingStep;
import ru.medvedev.importer.enums.FileStatus;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.exception.ColumnNamesNotFoundException;
import ru.medvedev.importer.exception.FileProcessingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final FileInfoService fileInfoService;
    private final FieldNameVariantService fieldNameVariantService;
    private final ApplicationEventPublisher eventPublisher;
    private final XlsxStorage xlsxStorage;
    private final HeaderProcessingService headerProcessingService;


    @Scheduled(cron = "${cron.launch-file-processing}")
    public void launchProcessFile() {
        if (fileInfoService.isExistsInProcess()) {
            return;
        }

        fileInfoService.getDownloadedFile().ifPresent(entity -> {
            log.debug("*** launch file processing [{}, id = {}]", entity.getName(), entity.getId());
            eventPublisher.publishEvent(new ImportEvent(this, "Файл взят в обработку",
                    EventType.LOG_TG, entity.getId(), true));
            entity = fileInfoService.changeStatus(entity, FileStatus.IN_PROCESS);
            xlsxStorage.setFileId(entity.getId());
            try {
                Map<XlsxRequireField, FieldNameVariantDto> namesMap = fieldNameVariantService.getAll();
                if (namesMap.keySet().stream().filter(field -> field != XlsxRequireField.TRASH)
                        .anyMatch(key -> namesMap.get(key).getNames().isEmpty() && namesMap.get(key).isRequired())) {
                    throw new ColumnNamesNotFoundException("Не указаны варианты названий полей", entity.getId());
                } else {
                    readFile(entity, namesMap);
                }
            } catch (FileProcessingException ex) {
                log.debug("Error processing file: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.ERROR,
                        ex.getFileId()));
            } catch (Exception ex) {
                log.debug("Error processing file: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                        .orElse("Непредвиденная ошибка"), EventType.ERROR,
                        entity.getId()));
            }
            xlsxStorage.setFileId(null);
            //new File(entity.getPath()).delete();
        });
    }

    /*private void readFile(String fileName, FileInputStream fis, Map<XlsxRequireField, FieldNameVariantDto> namesMap,
                          Long fileId)
            throws IOException {

        Workbook wb = fileName.endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);

        Sheet sheet = wb.getSheetAt(0);
        Map<XlsxRequireField, FieldPositionDto> fieldPositionMap = new HashMap<>();

        List<ContactEntity> contactBatch = new ArrayList<>();
        List<ContactEntity> resultContactList = new ArrayList<>();
        Map<String, InnRegionEntity> innRegionMap = innRegionService.getAllMap();
        regionCodes.clear();
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
        regionCodes.forEach(code -> eventPublisher.publishEvent(new ImportEvent(this,
                String.format("Регион с кодом %s не найден в справочнике", code), EventType.LOG, fileId)));
        wb.close();
        prepareContactToSkorozvon(resultContactList, fileId);
    }*/

    private void readFile(FileInfoEntity entity, Map<XlsxRequireField, FieldNameVariantDto> namesMap) {
        try {
            FileInputStream fis = new FileInputStream(new File(entity.getPath()));
            ColumnInfoDto columnInfoDto = headerProcessingService.headerProcessing(entity, namesMap, fis);
            entity.setColumnInfo(columnInfoDto);
            entity.setProcessingStep(FileProcessingStep.RESPONSE_COLUMN_NAME);
            fileInfoService.save(entity);
            fis.close();
        } catch (IOException e) {
            throw new FileProcessingException("Невозможно открыть файл", entity.getId());
        }
    }

    /*private Map<XlsxRequireField, FieldPositionDto> readHeader(Row row, Map<XlsxRequireField,
            FieldNameVariantDto> namesMap, Long fileId) {
        Map<XlsxRequireField, FieldPositionDto> positionField = new HashMap<>();
        namesMap.keySet().forEach(key -> {

            FieldPositionDto dto = new FieldPositionDto();
            FieldNameVariantDto fieldNameVariantDto = namesMap.get(key);
            dto.setRequired(fieldNameVariantDto.isRequired());

            for (Cell cell : row) {
                if (cell.getCellType() == CellType.BLANK) {
                    continue;
                }
                if (cell.getCellType() != CellType.STRING) {
                    eventPublisher.publishEvent(new ImportEvent(this, "В файле отсутствует шапка таблицы",
                            EventType.LOG_TG, fileId));
                    break;
                }

                if (fieldNameVariantDto.getNames().stream()
                        .anyMatch(name -> name.toLowerCase().equals(cell.getStringCellValue().toLowerCase()))) {
                    HeaderDto header = new HeaderDto();
                    header.setPosition(cell.getColumnIndex());
                    header.setValue(cell.getStringCellValue());
                    dto.getHeader().add(header);
                    break;
                }
            }

            if (dto.isRequired() && dto.getHeader().isEmpty()) {
                eventPublisher.publishEvent(new ImportEvent(this, String.format("Столбец %s не найден в файле",
                        key.getDescription()), EventType.LOG_TG, fileId));
            }
            positionField.put(key, dto);
        });

        //positionField.put(XlsxRequireField.TRASH, parseTrashColumns(row, positionField));
        return positionField;
    }*/

    /*private FieldPositionDto parseTrashColumns(Row row, Map<XlsxRequireField, FieldPositionDto> positionField) {
        List<Integer> usedPositions = positionField.keySet().stream()
                .flatMap(key -> positionField.get(key).getHeader().stream())
                .map(HeaderDto::getPosition)
                .collect(toList());

        FieldPositionDto fieldPositionDto = new FieldPositionDto();
        List<HeaderDto> headers = new ArrayList<>();
        for (Cell cell : row) {
            if (!usedPositions.contains(cell.getColumnIndex())) {
                HeaderDto headerDto = new HeaderDto();
                headerDto.setPosition(cell.getColumnIndex());
                headerDto.setValue(cell.getStringCellValue());
                headers.add(headerDto);
            }
        }
        fieldPositionDto.setHeader(headers);
        return fieldPositionDto;
    }*/

    /*private ContactEntity parseContact(Row row, Map<XlsxRequireField, FieldPositionDto> cellPositionMap, Long fileId,
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
                    case NAME:
                        getCellValue(cell, contact::setName, header, fileId);
                        break;
                    case SURNAME:
                        getCellValue(cell, contact::setSurname, header, fileId);
                        break;
                    case MIDDLE_NAME:
                        getCellValue(cell, contact::setMiddleName, header, fileId);
                        break;
                    case ORG_NAME:
                        try {
                            getCellValue(cell, contact::setOrgName, header, fileId);
                        } catch (IllegalCellTypeException ex) {
                            log.debug("*** OrgName is empty");
                        }
                        break;
                    case PHONE:
                        getCellValue(cell, val -> contact.setPhone(new BigDecimal(replaceSpecialCharacters(val))
                                .toString()), header, fileId);

                        break;
                    case INN:
                        getCellValue(cell, val -> contact.setInn(val.length() == 9 || val.length() == 11 ? "0" + val : val), header, fileId);
                        contact.setRegion(Optional.ofNullable(innRegionMap.get(contact.getInn().substring(0, 2)))
                                .map(InnRegionEntity::getName).orElseGet(() -> {
                                    regionCodes.add(contact.getInn().substring(0, 2));
                                    return "";
                                }));
                        break;
                    case OGRN:
                        getCellValue(cell, contact::setOgrn, header, fileId);
                        break;
                    case ADDRESS:
                        getCellValue(cell, contact::setAddress, header, fileId);
                        break;
                    case TRASH:
                        TrashColumnDto columnDto = new TrashColumnDto();
                        columnDto.setColumnName(header.getValue());
                        getCellValue(cell, columnDto::setValue, header, fileId);
                        trashColumns.add(columnDto);
                        break;
                }
            });
        });
        if (isBlank(contact.getOrgName())) {
            contact.setOrgName(String.format("%s %s %s", Optional.ofNullable(contact.getSurname()).orElse(""),
                    Optional.ofNullable(contact.getName()).orElse(""),
                    Optional.ofNullable(contact.getMiddleName()).orElse("")).trim());
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

    private void prepareContactToSkorozvon(List<ContactEntity> contacts, Long fileId) {
        List<LeadInfoResponse> positiveLead = new ArrayList<>();
        List<LeadInfoResponse> negativeLead = new ArrayList<>();

        for (int i = 0; i < contacts.size(); i = i + REQUEST_BATCH_SIZE) {
            List<ContactEntity> contactSublist = contacts.subList(i, Math.min(i + REQUEST_BATCH_SIZE, contacts.size()));
            vtbClientService.getAllFromCheckLead(contactSublist.stream()
                    .map(ContactEntity::getInn)
                    .collect(toList())).forEach(lead -> {
                if (lead.getResponseCode() == CheckLeadStatus.POSITIVE) {
                    positiveLead.add(lead);
                } else {
                    negativeLead.add(lead);
                }
            });
        }
        contactService.changeContactStatus(negativeLead, fileId, ContactStatus.REJECTED);
        sendContactToSkorozvon(contacts, positiveLead, fileId);
    }

    private void sendContactToSkorozvon(List<ContactEntity> contacts, List<LeadInfoResponse> leads, Long fileId) {

        String fileName = fileInfoService.getById(fileId).getName();
        Long projectNumber = projectNumberService.getNumberByDate(LocalDate.now());
        if (projectNumber == null) {
            throw new NumberProjectNotFoundException(String.format("Для даты %s не указан номер проекта",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))), fileId);
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
            skorozvonClientService.createMultiple(projectNumber,
                    orgList.subList(i, Math.min(i + REQUEST_BATCH_SIZE, orgList.size())),
                    Collections.singletonList(fileName));
        }
        contactService.changeContactStatus(leads, fileId, ContactStatus.DOWNLOADED);
    }

    private static CreateLeadDto xlsxRecordToLead(ContactEntity contact) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(String.format("%s %s %s", Optional.ofNullable(contact.getSurname()).orElse(""),
                Optional.ofNullable(contact.getName()).orElse(""),
                Optional.ofNullable(contact.getMiddleName()).orElse("")).trim());
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
        return val.replaceAll("[+*_()#\\-\"'$№%^&? ,]+", "");
    }*/
}
