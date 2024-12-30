package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.XMLReader;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.NotificationEvent;
import ru.medvedev.importer.entity.*;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateLeadException;
import ru.medvedev.importer.exception.TimeOutException;
import ru.medvedev.importer.service.bankclientservice.BankClientService;
import ru.medvedev.importer.service.bankclientservice.BankClientServiceFactory;
import ru.medvedev.importer.service.sheethandler.WebhookSheetHandler;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.enums.DownloadAction.ACTION_ALFA_SYSTEM_DOWNLOAD;
import static ru.medvedev.importer.enums.DownloadAction.ACTION_LEAD_DOWNLOAD;
import static ru.medvedev.importer.enums.FileInfoBankStatus.IN_QUEUE;
import static ru.medvedev.importer.enums.WebhookStatus.FIXED;
import static ru.medvedev.importer.enums.WebhookStatus.SUCCESS;
import static ru.medvedev.importer.service.ContactNewService.readDataFromSheet;
import static ru.medvedev.importer.service.EventService.NOTIFICATION_PATTERN;
import static ru.medvedev.importer.utils.StringUtils.getFioStringFromContact;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadWorkerService {

    private final XlsxParserService xlsxParserService;
    private final BankClientServiceFactory bankClientServiceFactory;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final WebhookStatisticService webhookStatisticService;
    private final ApplicationEventPublisher eventPublisher;
    private final FileInfoBankService fileInfoBankService;
    private final WebhookStatusService webhookStatusService;
    private final ContactNewService contactNewService;
    private final ContactDownloadInfoService contactDownloadInfoService;
    private final FileInfoService fileInfoService;
    private final EnabledScenarioService enabledScenarioService;
    private final SkorozvonClientService skorozvonClientService;

    @Scheduled(cron = "${cron.webhook-from-fixed-status}")
    public void fromStatusFixed() {

        webhookStatisticService.getByStatus(WebhookStatus.FIXED).ifPresent(item -> {
            log.debug("fromStatusFixed");
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            //мы спрашиваем у банка можем ли мы добавить лид
            try {
                if (item.getBank() == Bank.RAIFFEISEN) {
                    item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD);
                } else {
                    CheckLeadResult result = clientService.getAllFromCheckLead(item, Collections.singletonList(item.getInn()), null);
                    if (item.getBank() == Bank.VTB_OPENING) {
                        item.setStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE);
                        item.setOpeningRequestId(result.getAdditionalInfo());
                    } else {
                        if (result.getLeadResponse().stream().anyMatch(lead ->
                                lead.getResponseCode() == CheckLeadStatus.POSITIVE)) {
                            log.debug("*** have a positive lead inn {} ", item.getInn());
                            item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD);
                        } else {
                            eventPublisher.publishEvent(new NotificationEvent(this,
                                    String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                            result.getLeadResponse().get(0).getResponseCode(),
                                            result.getLeadResponse().get(0).getResponseCodeDescription(),
                                            item.getInn(),
                                            item.getCity(),
                                            Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                            Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                            Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                                    TelegramNotificationType.ALL,
                                    EventType.LOG_TG));
                            item.setStatus(WebhookStatus.REJECTED);
                        }
                    }
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM FIXED STATUS TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.FIXED);
            } catch (ErrorCheckLeadException ex) {
                log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
                eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки на дубликат. " +
                        ex.getMessage(),
                        EventType.LOG_TG, null));
                item.setStatus(WebhookStatus.ERROR);
            } catch (OperationNotSupportedException e) {
                log.debug("*** Operation not supported exception: fromStatusFixed {}", item);
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitCheckLeadAfterFixedResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE).ifPresent(item -> {
            log.debug("fromStatusWaitCheckLeadAfterFixedResponse");
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            try {
                CheckLeadResult result = clientService.getCheckLeadResult(item.getOpeningRequestId(), null);
                if (result.getStatus()) {
                    if (result.getLeadResponse().stream().anyMatch(lead ->
                            lead.getResponseCode() == CheckLeadStatus.POSITIVE)) {
                        log.debug("*** have a positive lead inn {} ", item.getInn());
                        item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD);
                    } else {
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                        result.getLeadResponse().get(0).getResponseCode(),
                                        result.getLeadResponse().get(0).getResponseCodeDescription(),
                                        item.getInn(),
                                        item.getCity(),
                                        Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                        Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                        Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                                TelegramNotificationType.ALL,
                                EventType.LOG_TG));
                        item.setStatus(WebhookStatus.REJECTED);
                    }
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM WAIT CHECK LEAD STATUS TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE);
            } catch (OperationNotSupportedException ex) {
                item.setStatus(WebhookStatus.ERROR);
            } catch (ErrorCheckLeadException ex) {

                log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Ошибка обработки заявки",
                                ex.getMessage(),
                                item.getInn(),
                                item.getCity(),
                                Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                        TelegramNotificationType.ADDING_ERROR,
                        EventType.LOG_TG));
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-from-try-to-create-status}")
    public void fromStatusTryToCreateLead() {

        webhookStatisticService.getByStatus(WebhookStatus.TRY_TO_CREATE_LEAD).ifPresent(item -> {
            log.debug("fromStatusTryToCreateLead");
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            WebhookLeadDto leadDto = new WebhookLeadDto();
            leadDto.setCity(item.getCity());
            leadDto.setInn(item.getInn());
            leadDto.setPhones(item.getPhone().split("\\|")[0]);
            leadDto.setEmails(isNotBlank(item.getEmail()) ? Collections.singletonList(item.getEmail()) : Collections.emptyList());
            /*SetProduct*/
            if (item.getBank() == Bank.ALFA) {
                if (item.getBankProduct() == null) {
                    item.setBankProduct(BankProduct.LP_RKO);
                } else {
                    Optional<BankProductEntity> bankProductOptional = item.getSuccessStatus().getProducts().stream()
                            .filter(bankProductEntity -> bankProductEntity.getCode() != BankProduct.LP_RKO)
                            .findFirst();
                    if (bankProductOptional.isPresent()) {
                        item.setBankProduct(bankProductOptional.get().getCode());
                    }
//                    else {
//                        item.setStatus(SUCCESS);
//                        webhookStatisticService.save(item);
//                        return;
//                    }
                }
                leadDto.setProduct(item.getBankProduct());
            }
            /**/
            Optional<ContactNewEntity> contactEntity = contactNewService.getByInn(item.getInn());
            if (contactEntity.isPresent()) {
                leadDto.setName(getFioStringFromContact(contactEntity.get()));
                leadDto.setOrgName(contactEntity.get().getOrgName());
            } else {
                leadDto.setName(isBlank(item.getFullName()) ? item.getName() : item.getFullName());
                leadDto.setOrgName(leadDto.getName());
            }
            try {
                if (item.getBank() == Bank.VTB_OPENING || item.getBank() == Bank.ALFA) {
                    leadDto.setComment(item.getComment());
                }
                CreateLeadResult result = clientService.createLead(leadDto);
                if (result.getStatus()) {
                    if (item.getBank() == Bank.VTB_OPENING || item.getBank() == Bank.ALFA) {
                        item.setStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE);
                        item.setOpeningRequestId(result.getAdditionalInfo());
                    } else if (item.getBank() == Bank.RAIFFEISEN) {
                        item.setStatus(SUCCESS);
                    } else {
                        item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                    }
                } else {
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                    isBlank(result.getAdditionalInfo()) ? "Непредвиденная ошибка" : result.getAdditionalInfo(),
                                    "",
                                    item.getInn(),
                                    item.getCity(),
                                    Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                    Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                    Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                            TelegramNotificationType.ADDING_ERROR,
                            EventType.LOG_TG));
                    item.setStatus(WebhookStatus.ERROR);
                }
            } catch (TimeOutException ex) {
                log.debug("*** CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.FIXED);
            } catch (ErrorCreateLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить",
                                ex.getMessage(),
                                item.getInn(),
                                item.getCity(),
                                Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                        TelegramNotificationType.ADDING_ERROR,
                        EventType.LOG_TG));
                item.setStatus(WebhookStatus.ERROR);
            } catch (OperationNotSupportedException e) {
                log.debug("*** Operation not supported exception: fromStatusTryToCreateLead {}", item);
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitToCreateLeadResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE).ifPresent(item -> {
            log.debug("fromStatusWaitToCreateLeadResponse");
            try {
                BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
                if (clientService.getCreateLeadResult(item)) {
                    item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM STATUS WAIT TO CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE);
            } catch (OperationNotSupportedException ex) {
                log.debug("*** Operation not supported exception {}", item);
                item.setStatus(WebhookStatus.ERROR);
            } catch (ErrorCreateLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить",
                                ex.getMessage(),
                                item.getInn(),
                                item.getCity(),
                                Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                        TelegramNotificationType.ADDING_ERROR,
                        EventType.LOG_TG));
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-from-create-success-status}")
    public void fromStatusTryToCreateLeadSuccess() {

        webhookStatisticService.getByStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS).ifPresent(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            try {
                CheckLeadResult result = clientService.getAllFromCheckLead(item, Collections.singletonList(item.getInn()), null);
                //и проверям добавился лид или нет
                if (item.getBank() == Bank.VTB_OPENING) {
                    item.setStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE);
                    item.setOpeningRequestId(result.getAdditionalInfo());
                } else {

                    if (item.getBank() == Bank.ALFA && item.getBankProduct() == BankProduct.LP_RKO && item.getSuccessStatus().getProducts().stream()
                            .anyMatch(bankProductEntity -> bankProductEntity.getCode() != BankProduct.LP_RKO)) {
                        item.setStatus(FIXED);

                    } else {
                        //todo костыль, что если после создания лида его можно создать еще раз
                    /*if (result.getLeadResponse().stream().anyMatch(lead ->
                            lead.getInn().equals(item.getInn()) && lead.getResponseCode() != CheckLeadStatus.POSITIVE)) {*/
                        log.debug("*** lead loaded in {} {} ", item.getBank(), item.getInn());
                        item.setStatus(SUCCESS);
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                        result.getLeadResponse().get(0).getResponseCode(),
                                        result.getLeadResponse().get(0).getResponseCodeDescription(),
                                        item.getInn(),
                                        item.getCity(),
                                        Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                        Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                        Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                                TelegramNotificationType.ALL,
                                EventType.LOG_TG));
                    /*} else {
                        item.setStatus(WebhookStatus.ERROR);
                    }*/
                    }
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM STATUS TRY TO CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
            } catch (OperationNotSupportedException e) {
                log.debug("*** Operation not supported exception: fromStatusTryToCreateLeadSuccess {}", item);
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitCheckLeadAfterCreateResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE).ifPresent(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());

            try {
                CheckLeadResult result = clientService.getCheckLeadResult(item.getOpeningRequestId(), null);
                if (result.getLeadResponse().stream().anyMatch(lead ->
                        lead.getInn().equals(item.getInn()) && lead.getResponseCode() != CheckLeadStatus.POSITIVE)) {
                    log.debug("*** lead loaded in {} {} ", item.getBank(), item.getInn());
                    item.setStatus(SUCCESS);
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                    result.getLeadResponse().get(0).getResponseCode(),
                                    result.getLeadResponse().get(0).getResponseCodeDescription(),
                                    item.getInn(),
                                    item.getCity(),
                                    Optional.ofNullable(item.getRegion()).map(InnRegionEntity::getName).orElse(""),
                                    Optional.ofNullable(item.getCityEntity()).map(CityEntity::getAlfaId).orElse(""),
                                    Optional.ofNullable(item.getContact()).map(ContactNewEntity::getOrgName).orElse(item.getName())),
                            TelegramNotificationType.ALL,
                            EventType.LOG_TG));
                } else {
                    item.setStatus(WebhookStatus.ERROR);
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM STATUS WAIT CHECK LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE);
            } catch (OperationNotSupportedException e) {
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    public void processWebhook(WebhookDto webhookDto) {
        //если вебхук имеет тип call_result
        if (webhookDto.getType().equals("call_result")) {
            String resultName = webhookDto.getCallResult().getResultName();
            //если результат соответствует нашим ожиданиям
            if (isNotBlank(resultName)) {
                WebhookStatusEntity webhookStatus = webhookStatusService.creatIfNotExists(resultName);

                //создаем заливку в банк
                Optional<WebhookSuccessStatusEntity> successStatusOptional = webhookSuccessStatusService.getByNameAndStatus(resultName);
                if (successStatusOptional.isPresent()) {
                    if (isNotBlank(webhookDto.getLead().getInn())) {
                        webhookStatisticService.addStatistic(WebhookStatus.FIXED, webhookDto, successStatusOptional.get());
                    } else {
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, successStatusOptional.get().getBank().getTitle(),
                                        "Невозможно добавить",
                                        "Некорректные данные",
                                        webhookDto.getLead().getInn(),
                                        webhookDto.getLead().getCity(),
                                        "",
                                        "",
                                        webhookDto.getLead().getName()),
                                TelegramNotificationType.ADDING_ERROR,
                                EventType.LOG_TG));
                    }
                }

                if (webhookDto.getCall().getScenarioId() != null) {
                    enabledScenarioService.findByScenarioId(webhookDto.getCall().getScenarioId()).ifPresent(scenario ->
                            contactNewService.updateActuality(webhookDto.getLead().getInn(), scenario.getBank(), webhookStatus));
                }
            }
        }
    }

    public void massLoadWebhooks(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {

            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
            XMLReader parser = XMLHelper.newXMLReader();
            WebhookSheetHandler handler = new WebhookSheetHandler(sst, skorozvonClientService.getScenarioList().stream()
                    .collect(toMap(ScenarioDto::getName, item -> item)));
            parser.setContentHandler(handler);
            readDataFromSheet(parser, r);
            handler.getWebhooks().forEach(this::processWebhook);
        } catch (Exception e) {
            log.error("Cannot read excel-file '{}'", file.getName(), e);
        }
    }

    //обработка файла загрженного с интерфейса
    public void processXlsxRecords(FileInfoEntity fileInfo) throws IOException {

        eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                EventType.FILE_PROCESS, fileInfo.getId()));
        Map<String, List<XlsxRecordDto>> records = getValidInnListFromFile(fileInfo);
        saveNewContacts(records, fileInfo);
        //запуск обработки по разным банкам
        if (!fileInfo.getBankList().isEmpty() && fileInfo.getAction() == ACTION_LEAD_DOWNLOAD) {
            contactDownloadInfoService.createDownloadByBanks(new ArrayList<>(records.keySet()), fileInfo.getBankList());
            fileInfo.getBankList().forEach(fileBank ->
                    fileInfoBankService.updateDownloadStatus(IN_QUEUE, fileBank.getId()));
        }
    }

    public void processXlsxRecords(FileInfoEntity fileInfo, Set<String> innList) throws IOException {

        eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                EventType.FILE_PROCESS, fileInfo.getId()));

        //запуск обработки по разным банкам
        if (!fileInfo.getBankList().isEmpty()) {
            fileInfoBankService.save(fileInfo.getBankList());
            contactDownloadInfoService.createDownloadByBanks(new ArrayList<>(innList), fileInfo.getBankList());
            fileInfo.getBankList().forEach(fileBank -> fileBank.setDownloadStatus(IN_QUEUE));
            fileInfoService.save(fileInfo);
        }
    }

    private Map<String, List<XlsxRecordDto>> getValidInnListFromFile(FileInfoEntity fileInfo) throws IOException {
        return xlsxParserService.readColumnBody(fileInfo).stream()
                .filter(item -> isNotBlank(item.getOrgInn()))
                .filter(item -> item.getOrgInn().length() == 10 || item.getOrgInn().length() == 12)
                .collect(groupingBy(XlsxRecordDto::getOrgInn));
    }

    private void saveNewContacts(Map<String, List<XlsxRecordDto>> records, FileInfoEntity fileInfo) {
        List<ContactNewEntity> contacts = new ArrayList<>();
        records.values()
                .stream()
                .map(value -> value.get(0))
                .forEach(record -> {
                    Optional<ContactNewEntity> contactNewOptional = contactNewService.getByInn(record.getOrgInn());
                    if (contactNewOptional.isPresent() && fileInfo.getAction() == ACTION_ALFA_SYSTEM_DOWNLOAD) {
                        ContactNewEntity contactNewEntity = contactNewOptional.get();
                        contactNewEntity.setAlfaCreateAt(record.getCreateAt() == null ? LocalDateTime.now() : record.getCreateAt());
                        contacts.add(contactNewEntity);
                    } else if (!contactNewOptional.isPresent()) {
                        String[] fioSplit = record.getFio().split(" ");
                        ContactNewEntity contact = new ContactNewEntity();
                        contact.setCity(isNotBlank(record.getCity()) ? record.getCity() : record.getOrgCity());
                        contact.setOrgName(record.getOrgName());
                        contact.setInn(record.getOrgInn());
                        contact.setRegion(isNotBlank(record.getRegion()) ? record.getRegion() : record.getOrgRegion());
                        contact.setName(fioSplit.length >= 2 ? fioSplit[1] : null);
                        contact.setSurname(fioSplit.length >= 1 ? fioSplit[0] : null);
                        contact.setMiddleName(fioSplit.length >= 3 ? fioSplit[2] : null);
                        contact.setOgrn(record.getOrgKpp());
                        contact.setPhone(isNotBlank(record.getPhone()) ? record.getPhone() : record.getOrgPhone());
                        contact.setCreateAt(record.getCreateAt());
                        contact.setFileInfoId(fileInfo.getId());

                        if (fileInfo.getAction() == ACTION_ALFA_SYSTEM_DOWNLOAD) {
                            contact.setAlfaCreateAt(record.getCreateAt() == null ? LocalDateTime.now() : record.getCreateAt());
                        }
                        contacts.add(contact);
                    }
                });
        contactNewService.save(contacts.stream()
                .filter(contact -> checkLength(contact.getOrgName(), 1000)
                        && checkLength(contact.getName(), 100)
                        && checkLength(contact.getSurname(), 100)
                        && checkLength(contact.getMiddleName(), 100)
                        && checkLength(contact.getPhone(), 20)
                        && checkLength(contact.getInn(), 20)
                        && checkLength(contact.getOgrn(), 20)
                        && checkLength(contact.getRegion(), 500)
                        && checkLength(contact.getCity(), 1000))
                .collect(Collectors.toList()));
    }

    private boolean checkLength(String value, int length) {
        return value == null || value.length() <= length;
    }
}
