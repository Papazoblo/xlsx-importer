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
import ru.medvedev.importer.exception.TimeOutException;
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
import static ru.medvedev.
import static ru.medvedev.importer.enums.RequestStatus.ERROR;
import static ru.medvedev.importer.enums.RequestStatus.IN_QUEUE;
importer.enums.RequestStatus.*;
import static ru.medvedev.importer.service.EventService.BANK_NAME_PATTERN;
import static ru.medvedev.importer.service.EventService.STATISTIC_LINE_PATTERN;
import static ru.medvedev.importer.utils.StringUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BodyProcessingService {

    private static final Integer BATCH_SIZE = 100;
    private static final Integer MAX_TRY_RESEND = 5;

    private final FileInfoService fileInfoService;
    private final ContactService contactService;
    private final SkorozvonClientService skorozvonClientService;
    private final BankClientServiceFactory bankClientServiceFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final InnRegionService innRegionService;
    private final DownloadFilterService downloadFilterService;
    private final ObjectMapper objectMapper;
    private final FileInfoBankService fileInfoBankService;
    private final RequestService requestService;
    private final ContactDownloadInfoService contactDownloadInfoService;

    private final Set<String> regionCodes = new HashSet<>();

    @Scheduled(cron = "${cron.tg-file-body-processor}")
    public void sendRequestToTelegram() {
        //todo загрузку файлов с телеграма надо переделать под новую загрузку
        fileInfoService.getFileToProcessingBody().ifPresent(file -> {
            eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                    EventType.FILE_PROCESS, file.getId()));

            file.setProcessingStep(FileProcessingStep.READ_DATA);
            fileInfoService.save(file);

            try {
                List<List<ContactEntity>> contactBatchList = readValidContactFromFile(file);
                file.getBankList().forEach(fileBank -> {
                    //todo переделать
                    /*contactBatchList.forEach(batch ->
                            contactService.filteredContacts(batch, fileBank));*/
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
                    EventType.FILE_PROCESS, fib.getFileInfoId()));
            try {
                fileInfoBankService.updateDownloadStatus(FileInfoBankStatus.INN_CHECK, fib.getId());
                List<ContactDownloadInfoEntity> contacts = fib.getContactDownloadList();/*.stream()
                        .map(ContactDownloadInfoEntity::getContact)
//                        .filter(contact -> contact.getStatus() == ContactStatus.IN_CHECK)
                        .collect(toList());*/

                for (int i = 0; i < contacts.size(); i = i + BATCH_SIZE) {
                    List<ContactDownloadInfoEntity> contactSublist = contacts.subList(i, Math.min(i + BATCH_SIZE, contacts.size()));

                    RequestEntity openingRequest = new RequestEntity();
                    openingRequest.setFileInfoBank(fib);
                    openingRequest = requestService.save(openingRequest);
                    for(ContactDownloadInfoEntity info : contactSublist) {
                        info.setRequestId(openingRequest.getId());
                    }
                    contactDownloadInfoService.save(contactSublist);
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

    @Scheduled(cron = "${cron.creating-request-vtb}")
    public void creatingRequestVtb() {
        requestService.getFirstByStatusAndBank(CREATING, VTB).ifPresent(this::creatingRequest);
    }

    @Scheduled(cron = "${cron.creating-request-opening}")
    public void creatingRequestOpening() {
        requestService.getFirstByStatusAndBank(CREATING, VTB_OPENING).ifPresent(this::creatingRequest);
    }

    private void creatingRequest(RequestEntity request) {
        FileInfoBankEntity fib = request.getFileInfoBank();
        try {
            BankClientService bankClientService = bankClientServiceFactory.getBankClientService(fib.getBank());

            request.setStatus(IN_QUEUE);
            if (fib.getBank() == VTB) {
                request.setRequestId("blank");
            } else {
                CheckLeadResult result = bankClientService.getAllFromCheckLead(request.getContactDownloadInfo().stream()
                        .map(ContactDownloadInfoEntity::getContact)
                        .map(ContactNewEntity::getInn)
                        .collect(toList()), fib.getFileInfoId());
                request.setRequestId(result.getAdditionalInfo());

                if (!result.getStatus()) {
                    request.setStatus(ERROR);
                }
            }
        } catch (TimeOutException ex) {
            log.debug("Timeout error create request to check: {}", ex.getMessage());
            if (request.incRetryRequestCount() <= MAX_TRY_RESEND) {
                request.setStatus(CREATING);
            } else {
                request.setStatus(ERROR);
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                        request.getFileInfoBank().getFileInfoId()));
            }
        } catch (FileProcessingException ex) {
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
            if (request.incRetryRequestCount() <= MAX_TRY_RESEND) {
                request.setStatus(IN_QUEUE);
            }
        }
        requestService.save(request);
    }

    @Scheduled(cron = "${cron.check-request-status-vtb}")
    public void checkRequestStatusVtb() {
        requestService.getFirstByStatusAndBank(IN_QUEUE, VTB).ifPresent(this::checkRequestStatus);
    }

    @Scheduled(cron = "${cron.check-request-status-opening}")
    public void checkRequestStatusOpening() {
        requestService.getFirstByStatusAndBank(IN_QUEUE, VTB_OPENING).ifPresent(this::checkRequestStatus);
    }

    private void checkRequestStatus(RequestEntity request) {
        //запуск обработки по разным банкам
        //openingRequestService.getFirstByStatus(CHECKING);
        FileInfoBankEntity fib = request.getFileInfoBank();

        /*eventPublisher.publishEvent(new ImportEvent(this, "Проверяю группу для `" +
                fib.getBank().getTitle() + "`",
                EventType.LOG_TG, fib.getFileInfoId()));*/

        try {
            BankClientService bankClientService = bankClientServiceFactory.getBankClientService(fib.getBank());

            if (fib.getBank() == VTB_OPENING) {
                CheckLeadResult result = bankClientService.getCheckLeadResult(request.getRequestId(), fib.getFileInfoId());
                if (result.getStatus()) {
                    result.getLeadResponse().forEach(lead -> request.getContactDownloadInfo().forEach(downloadInfo -> {
                        if (downloadInfo.getContact().getInn().equals(lead.getInn())) {
                            downloadInfo.setCheckStatus(lead.getResponseCode() == CheckLeadStatus.POSITIVE
                                    ? ContactStatus.DOWNLOADED : ContactStatus.REJECTED);
                        }
                    }));
                    request.setStatus(SUCCESS_CHECK);
                } else {
                    request.setStatus(IN_QUEUE);
                }
            } else {
                CheckLeadResult result = bankClientService.getAllFromCheckLead(request.getContactDownloadInfo().stream()
                        .map(ContactDownloadInfoEntity::getContact)
                        .map(ContactNewEntity::getInn)
                        .collect(toList()), fib.getFileInfoId());
                request.getContactDownloadInfo().forEach(contact -> result.getLeadResponse().forEach(lead -> {
                    if (contact.getContact().getInn().equals(lead.getInn())) {
                        contact.setCheckStatus(lead.getResponseCode() == CheckLeadStatus.POSITIVE
                                ? ContactStatus.DOWNLOADED : ContactStatus.REJECTED);
                    }
                }));
                request.setStatus(SUCCESS_CHECK);
            }

        } catch (TimeOutException ex) {
            log.debug("Timeout error check request status: {}", ex.getMessage());
            request.setStatus(ERROR);
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
            if (request.incRetryRequestCount() <= MAX_TRY_RESEND) {
                request.setStatus(IN_QUEUE);
            }
        }
        requestService.save(request);
        /*if (request.getStatus() == SUCCESS_CHECK) {
            sendToSkorozvon(request);
        }*/
    }

    @Scheduled(cron = "${cron.tg-file-send-to-skorozvon}")
    public void sendToSkorozvon(/*OpeningRequestEntity request*/) {
        //запуск обработки по разным банкам
        requestService.getFirstByStatus(RequestStatus.SUCCESS_CHECK).ifPresent(request -> {
            requestService.changeStatus(request.getId(), RequestStatus.DOWNLOADING);
            FileInfoBankEntity fib = request.getFileInfoBank();

            eventPublisher.publishEvent(new ImportEvent(this, getFileStatisticString(fib.getFileInfoId()),
                    EventType.FILE_PROCESS, fib.getFileInfoId()));
            try {
                FileInfoEntity fileInfo = request.getFileInfoBank().getFileInfo();

                if (fib.getProjectId() == null) {
                    throw new NumberProjectNotFoundException("Не указан номер проекта", fileInfo.getId());
                }

                List<CreateOrganizationDto> orgList = request.getContactDownloadInfo().stream()
                        .filter(downloadInfo -> downloadInfo.getCheckStatus() == ContactStatus.DOWNLOADED)
                        .map(ContactDownloadInfoEntity::getContact)
                        .collect(groupingBy(ContactNewEntity::getInn)).values()
                        .stream()
                        .map(contactList -> {
                            CreateOrganizationDto orgDto = xlsxRecordToOrganization(contactList.get(0), fib.getBank());
                            contactList.forEach(contact -> orgDto.getLeads().add(xlsxRecordToLead(contact)));
                            return orgDto;
                        }).collect(toList());

                skorozvonClientService.createMultiple(fib.getProjectId(),
                        orgList,
                        Collections.singletonList(fileInfo.getName()));

                requestService.changeStatus(request.getId(), RequestStatus.DOWNLOADED);
            } catch (TimeOutException ex) {
                if (request.incRetryRequestCount() <= MAX_TRY_RESEND) {
                    log.debug("Timeout error send to skorozvon: {}", ex.getMessage());
                    request.setStatus(SUCCESS_CHECK);
                } else {
                    request.setStatus(ERROR);
                    log.debug("Error send to skorozvon: {}", ex.getMessage());
                    eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                            request.getFileInfoBank().getFileInfoId()));
                }
                requestService.save(request);
            } catch (FileProcessingException ex) {
                log.debug("Error send to skorozvon: {}", ex.getMessage());
                eventPublisher.publishEvent(new ImportEvent(this, ex.getMessage(), EventType.LOG,
                        ex.getFileId()));
                requestService.changeStatus(request.getId(), ERROR);
            } catch (Exception ex) {
                log.debug("Error send to skorozvon", ex);
                eventPublisher.publishEvent(new ImportEvent(this, Optional.ofNullable(ex.getMessage())
                        .orElse("Непредвиденная ошибка\n" + ex.getClass()), EventType.LOG,
                        fib.getFileInfoId()));
                requestService.changeStatus(request.getId(), ERROR);
            }
        });
    }

    @Scheduled(cron = "${cron.ending-file-processing}")
    public void endingFileProcessing() {

        fileInfoService.getWaitingFile().forEach(file -> {
            log.debug("*** try ending file processing [{}, id = {}]", file.getName(), file.getId());

            file.getBankList().forEach(fib -> {
                if (Stream.of(WAIT_CHECK_FINISH, SEND_TO_SKOROZVON, CHECK_FINISH)
                        .anyMatch(status -> status == fib.getDownloadStatus())) {
                    if (fib.getContactDownloadList().stream()
                            .map(ContactDownloadInfoEntity::getRequest)
                            .distinct()
                            .collect(toList()).stream()
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

                eventPublisher.publishEvent(new ImportEvent(this, "Обработка завершена\n" +
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

    private ContactEntity parseContact(Row row, Map<XlsxRequireField, FieldPositionDto> cellPositionMap, Long
            fileId,
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

    private static CreateLeadDto xlsxRecordToLead(ContactNewEntity contact) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(getFioStringFromContact(contact));
        lead.setPhones(Collections.singletonList(contact.getPhone()));
        lead.setCity(contact.getCity());
        lead.setRegion(contact.getRegion());
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(ContactNewEntity contact, Bank bank) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        String fioStringFromContact = getFioStringFromContact(contact);
        organization.setName(String.format("%s %s %s", bank.getTitle(), contact.getOrgName().contains(fioStringFromContact) ? "" : fioStringFromContact, contact.getOrgName()));
        organization.setPhones(Collections.singletonList(contact.getPhone()));
        organization.setHomepage(String.format("https://wa.me/%s", contact.getPhone()));
        organization.setCity(contact.getCity());
        organization.setRegion(contact.getRegion());
        organization.setInn(contact.getInn());
        organization.setComment(contact.getOgrn());
        return organization;
    }

    private String getFileStatisticString(Long fileId) {
        return requestService.getStatisticByFileId(fileId).entrySet().stream()
                .map(outEntry -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format(BANK_NAME_PATTERN, outEntry.getKey().getTitle()));
                    sb.append(String.format(STATISTIC_LINE_PATTERN, "Всего",
                            outEntry.getValue().values().stream().mapToLong(l -> l).sum()));
                    Arrays.stream(RequestStatus.values()).forEach(status ->
                            sb.append(String.format(STATISTIC_LINE_PATTERN, status.getTitle(),
                                    Optional.ofNullable(outEntry.getValue().get(status)).orElse(0))));
                    return sb.toString();
                })
                .collect(joining("\n"));
    }
}
