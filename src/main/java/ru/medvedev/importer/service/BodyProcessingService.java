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
import ru.medvedev.importer.entity.*;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.exception.FileProcessingException;
import ru.medvedev.importer.exception.IllegalCellTypeException;
import ru.medvedev.importer.exception.NumberProjectNotFoundException;
import ru.medvedev.importer.service.bankclientservice.BankClientService;
import ru.medvedev.importer.service.bankclientservice.BankClientServiceFactory;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.enums.Bank.VTB;
import static ru.medvedev.importer.enums.Bank.VTB_OPENING;
import static ru.medvedev.importer.enums.FileInfoBankStatus.*;
import static ru.medvedev.importer.enums.OpeningRequestStatus.*;
import static ru.medvedev.importer.enums.OpeningRequestStatus.ERROR;
import static ru.medvedev.importer.enums.OpeningRequestStatus.IN_QUEUE;

import static ru.medvedev.importer.utils.StringUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BodyProcessingService {

    private static final Integer BATCH_SIZE = 100;

    private final FileInfoService fileInfoService;
    private final ContactService contactService;
    private final SkorozvonClientService skorozvonClientService;
    private final BankClientServiceFactory bankClientServiceFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final InnRegionService innRegionService;
    private final DownloadFilterService downloadFilterService;
    private final ObjectMapper objectMapper;
    private final FileInfoBankService fileInfoBankService;
    private final OpeningRequestService openingRequestService;

    private final Set<String> regionCodes = new HashSet<>();

    @Scheduled(cron = "${cron.tg-file-body-processor}")
    public void sendRequestToTelegram() {
        fileInfoService.getFileToProcessingBody().ifPresent(file -> {
            eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                    EventType.LOG_TG, file.getId()));

            file.setProcessingStep(FileProcessingStep.READ_DATA);
            fileInfoService.save(file);

            try {
                List<List<ContactEntity>> contactBatchList = readValidContactFromFile(file);
                file.getBankList().forEach(fileBank -> {
                    contactBatchList.forEach(batch ->
                            contactService.filteredContacts(batch, fileBank));
                    fileBank.setDownloadStatus(FileInfoBankStatus.IN_QUEUE);
                    fileInfoBankService.save(fileBank);
                });
                contactBatchList.clear();
                fileInfoService.changeStatus(file, FileStatus.WAITING);
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

    @Scheduled(cron = "${cron.tg-file-send-to-check}")
    public void processFile() {
        //запуск обработки по разным банкам
        fileInfoBankService.getByDownloadStatus(FileInfoBankStatus.IN_QUEUE).ifPresent(fib -> {

            eventPublisher.publishEvent(new ImportEvent(this, "Разбиваю контакты по группам в `" +
                    fib.getBank().getTitle() + "`",
                    EventType.LOG_TG, fib.getFileInfoId()));
            try {
                fileInfoBankService.updateDownloadStatus(FileInfoBankStatus.INN_CHECK, fib.getId());
                List<ContactEntity> contacts = fib.getContacts().stream()
                        .filter(contact -> contact.getStatus() == ContactStatus.IN_CHECK)
                        .collect(toList());

                for (int i = 0; i < contacts.size(); i = i + BATCH_SIZE) {
                    List<ContactEntity> contactSublist = contacts.subList(i, Math.min(i + BATCH_SIZE, contacts.size()));

                    OpeningRequestEntity openingRequest = new OpeningRequestEntity();
                    openingRequest.setFileInfoBank(fib);
                    if (fib.getBank() == VTB) {
                        openingRequest.setRequestId("blank");
                    } else {
                        CheckLeadResult result = bankClientServiceFactory.getBankClientService(fib.getBank())
                                .getAllFromCheckLead(contactSublist.stream()
                                        .map(ContactEntity::getInn)
                                        .collect(toList()), fib.getFileInfoId());
                        openingRequest.setRequestId(result.getAdditionalInfo());
                    }
                    //contactSublist.forEach(contact -> contact.setOpeningRequest(openingRequest));
                    openingRequest.setContacts(contactSublist);
                    fib.getOpeningRequests().add(openingRequest);
                }

                fib.setDownloadStatus(WAIT_CHECK_FINISH);
                fileInfoBankService.save(fib);
                fileInfoService.changeStatus(fib.getFileInfo(), FileStatus.WAITING_CHECK);

            } catch (FileProcessingException ex) {
                log.debug("Error split contact to batch: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                        ex.getFileId()));
            } catch (Exception ex) {
                log.debug("Error split contact to batch", ex);
                eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                        .orElse("Непредвиденная ошибка\n" + ex.getClass()), EventType.LOG,
                        fib.getFileInfoId()));
            }
        });
    }

    @Scheduled(cron = "${cron.check-request-status-vtb}")
    public void checkRequestStatusVtb() {
        openingRequestService.getFirstByStatusAndBank(IN_QUEUE, VTB).ifPresent(this::checkRequestStatus);
    }

    @Scheduled(cron = "${cron.check-request-status-opening}")
    public void checkRequestStatusOpening() {
        openingRequestService.getFirstByStatusAndBank(IN_QUEUE, VTB_OPENING).ifPresent(this::checkRequestStatus);
    }

    private void checkRequestStatus(OpeningRequestEntity request) {
        //запуск обработки по разным банкам
        openingRequestService.getFirstByStatus(CHECKING);
        FileInfoBankEntity fib = request.getFileInfoBank();

        eventPublisher.publishEvent(new ImportEvent(this, "Проверяю группу для `" +
                fib.getBank().getTitle() + "`",
                EventType.LOG_TG, fib.getFileInfoId()));

        try {
            BankClientService bankClientService = bankClientServiceFactory.getBankClientService(fib.getBank());

            if (fib.getBank() == VTB_OPENING) {
                CheckLeadResult result = bankClientService.getCheckLeadResult(request.getRequestId(), fib.getFileInfoId());
                if (result.getStatus()) {
                    result.getLeadResponse().forEach(lead -> request.getContacts().forEach(contact -> {
                        if (contact.getInn().equals(lead.getInn())) {
                            contact.setStatus(lead.getResponseCode() == CheckLeadStatus.POSITIVE
                                    ? ContactStatus.DOWNLOADED : ContactStatus.REJECTED);
                        }
                    }));
                    request.setStatus(SUCCESS_CHECK);
                } else {
                    request.setStatus(IN_QUEUE);
                }
            } else {
                CheckLeadResult result = bankClientService.getAllFromCheckLead(request.getContacts().stream()
                        .map(ContactEntity::getInn)
                        .collect(toList()), fib.getFileInfoId());
                request.getContacts().forEach(contact -> result.getLeadResponse().forEach(lead -> {
                    if (contact.getInn().equals(lead.getInn())) {
                        contact.setStatus(lead.getResponseCode() == CheckLeadStatus.POSITIVE
                                ? ContactStatus.DOWNLOADED : ContactStatus.REJECTED);
                    }
                }));
                request.setStatus(SUCCESS_CHECK);
            }
        } catch (FileProcessingException | OperationNotSupportedException ex) {
            log.debug("*** Error check lead exception", ex);
            eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                    fib.getFileInfoId()));
            request.setStatus(ERROR);
        } catch (Exception ex) {
            log.debug("Error check lead exception", ex);
            eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                    .orElse("Непредвиденная ошибка\n" + ex.getClass()), EventType.LOG,
                    fib.getFileInfoId()));
            request.setStatus(ERROR);
        }
        if (request.getStatus() == ERROR) {
            if (request.incRetryRequestCount() <= 3) {
                request.setStatus(IN_QUEUE);
            }
        }
        openingRequestService.save(request);
        if (request.getStatus() == SUCCESS_CHECK) {
            sendToSkorozvon(request);
        }
    }

    //@Scheduled(cron = "${cron.tg-file-send-to-skorozvon}")
    public void sendToSkorozvon(OpeningRequestEntity request) {
        //запуск обработки по разным банкам
        //openingRequestService.getFirstByStatus(OpeningRequestStatus.SUCCESS_CHECK).ifPresent(request -> {
        openingRequestService.changeStatus(request.getId(), OpeningRequestStatus.DOWNLOADING);
        FileInfoBankEntity fib = request.getFileInfoBank();

        eventPublisher.publishEvent(new ImportEvent(this, "Отправляю группу в скорозвон `" +
                fib.getBank().getTitle() + "`",
                EventType.LOG_TG, fib.getFileInfoId()));
        try {
            FileInfoEntity fileInfo = request.getFileInfoBank().getFileInfo();

            if (fib.getProjectId() == null) {
                throw new NumberProjectNotFoundException("Не указан номер проекта", fileInfo.getId());
            }

            List<CreateOrganizationDto> orgList = request.getContacts().stream()
                    .filter(contact -> contact.getStatus() == ContactStatus.DOWNLOADED)
                    .collect(groupingBy(ContactEntity::getInn)).values()
                    .stream()
                    .map(contactList -> {
                        CreateOrganizationDto orgDto = xlsxRecordToOrganization(contactList.get(0));
                        contactList.forEach(contact -> orgDto.getLeads().add(xlsxRecordToLead(contact)));
                        return orgDto;
                    }).collect(toList());

            skorozvonClientService.createMultiple(fib.getProjectId(),
                    orgList,
                    Collections.singletonList(fileInfo.getName()));

            openingRequestService.changeStatus(request.getId(), OpeningRequestStatus.DOWNLOADED);
        } catch (FileProcessingException ex) {
            log.debug("Error send to skorozvon: {}", ex.getMessage());
            eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                    ex.getFileId()));
            openingRequestService.changeStatus(request.getId(), ERROR);
        } catch (Exception ex) {
            log.debug("Error send to skorozvon", ex);
            eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                    .orElse("Непредвиденная ошибка\n" + ex.getClass()), EventType.LOG,
                    fib.getFileInfoId()));
            openingRequestService.changeStatus(request.getId(), ERROR);
        }
        //});
    }

    @Scheduled(cron = "${cron.ending-file-processing}")
    public void endingFileProcessing() {

        fileInfoService.getWaitingFile().forEach(file -> {
            log.debug("*** try ending file processing [{}, id = {}]", file.getName(), file.getId());

            file.getBankList().forEach(fib -> {
                if (Stream.of(WAIT_CHECK_FINISH, SEND_TO_SKOROZVON, CHECK_FINISH)
                        .anyMatch(status -> status == fib.getDownloadStatus())) {
                    if (fib.getOpeningRequests().stream()
                            .allMatch(request -> request.getStatus() == ERROR
                                    || request.getStatus() == DOWNLOADED)) {
                        fib.setDownloadStatus(SUCCESS);
                    }
                }
            });

            if (file.getBankList().stream()
                    .allMatch(fib -> fib.getDownloadStatus() == SUCCESS ||
                            fib.getDownloadStatus() == FileInfoBankStatus.ERROR)) {
                file.setStatus(FileStatus.SUCCESS);
            }

            if (file.getStatus() == FileStatus.SUCCESS) {

                eventPublisher.publishEvent(new ImportEvent(this, "Обработка заверщена\n" +
                        fileInfoBankService.getDownloadStatistic(file.getId()).entrySet().stream()
                                .map(entry -> entry.getKey().getTitle() + ": " + entry.getValue())
                                .collect(Collectors.joining("\n")),
                        EventType.SUCCESS, file.getId()));
            }
        });
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
        organization.setName(String.format("%s %s %s", contact.getBank().getTitle(), getFioStringFromContact(contact), contact.getOrgName()));
        organization.setPhones(Collections.singletonList(contact.getPhone()));
        organization.setHomepage(String.format("https://api.whatsapp.com/send?phone=%s", contact.getPhone()));
        organization.setCity(contact.getCity());
        organization.setRegion(contact.getRegion());
        organization.setInn(contact.getInn());
        organization.setComment(contact.getOgrn());
        return organization;
    }
}
