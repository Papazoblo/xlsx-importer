package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.NotificationEvent;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.WebhookStatusEntity;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;
import ru.medvedev.importer.exception.TimeOutException;
import ru.medvedev.importer.service.bankclientservice.BankClientService;
import ru.medvedev.importer.service.bankclientservice.BankClientServiceFactory;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static ru.medvedev.importer.enums.FileInfoBankStatus.IN_QUEUE;
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
    //private final ContactService contactService;
    private final ApplicationEventPublisher eventPublisher;
    private final DownloadFilterService downloadFilterService;
    private final FileInfoBankService fileInfoBankService;
    private final WebhookStatusService webhookStatusService;
    private final SkorozvonClientService skorozvonClientService;
    private final ContactNewService contactNewService;
    private final ContactDownloadInfoService contactDownloadInfoService;
    private final FileInfoService fileInfoService;
    private final EnabledScenarioService enabledScenarioService;

    @Scheduled(cron = "${cron.webhook-from-fixed-status}")
    public void fromStatusFixed() {

        webhookStatisticService.getByStatus(WebhookStatus.FIXED).ifPresent(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            //мы спрашиваем у банка можем ли мы добавить лид
            try {
                CheckLeadResult result = clientService.getAllFromCheckLead(Collections.singletonList(item.getInn()), null);
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
                                        String.format("Заявка отклонена (%s)", result.getLeadResponse().get(0)
                                                .getResponseCode()),
                                        item.getInn(),
                                        item.getCity(),
                                        item.getName()),
                                EventType.LOG_TG));
                        item.setStatus(WebhookStatus.REJECTED);
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
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitCheckLeadAfterFixedResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE).ifPresent(item -> {
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
                                        String.format("Заявка отклонена (%s)", result.getLeadResponse().get(0)
                                                .getResponseCode()),
                                        item.getInn(),
                                        item.getCity(),
                                        item.getName()),
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
                                String.format("Ошибка обработки заявки (%s)", ex.getMessage()),
                                item.getInn(),
                                item.getCity(),
                                item.getName()),
                        EventType.LOG_TG));
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-from-try-to-create-status}")
    public void fromStatusTryToCreateLead() {

        webhookStatisticService.getByStatus(WebhookStatus.TRY_TO_CREATE_LEAD).ifPresent(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            WebhookLeadDto leadDto = new WebhookLeadDto();
            leadDto.setCity(item.getCity());
            leadDto.setInn(item.getInn());
            leadDto.setPhones(item.getPhone().split("\\|")[0]);
            leadDto.setEmails(isNotBlank(item.getEmail()) ? Collections.singletonList(item.getEmail()) : Collections.emptyList());
            Optional<ContactNewEntity> contactEntity = contactNewService.getByInn(item.getInn());
            if (contactEntity.isPresent()) {
                leadDto.setName(getFioStringFromContact(contactEntity.get()));
            } else {
                leadDto.setName(isBlank(item.getFullName()) ? item.getName() : item.getFullName());
            }
            try {
                if (item.getBank() == Bank.VTB_OPENING) {
                    leadDto.setComment(item.getComment());
                }
                CreateLeadResult result = clientService.createLead(leadDto);
                if (result.getStatus()) {
                    if (item.getBank() == Bank.VTB_OPENING) {
                        item.setStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE);
                        item.setOpeningRequestId(result.getAdditionalInfo());
                    } else {
                        item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                    }
                } else {
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Непредвиденная ошибка", leadDto.getInn(), leadDto.getCity(), leadDto.getName()),
                            EventType.LOG_TG));
                    item.setStatus(WebhookStatus.ERROR);
                }
            } catch (TimeOutException ex) {
                log.debug("*** CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.FIXED);
            } catch (ErrorCreateVtbLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить. " + ex.getMessage(), leadDto.getInn(), leadDto.getCity(), leadDto.getName()),
                        EventType.LOG_TG));
                item.setStatus(WebhookStatus.ERROR);
            }
            webhookStatisticService.save(item);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitToCreateLeadResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE).ifPresent(item -> {
            try {
                BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
                if (clientService.getCreateLeadResult(item.getOpeningRequestId())) {
                    item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM STATUS WAIT TO CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE);
            } catch (OperationNotSupportedException ex) {
                log.debug("*** Operation not supported exception {}", item);
                item.setStatus(WebhookStatus.ERROR);
            } catch (ErrorCreateVtbLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить. " + ex.getMessage(),
                                item.getInn(),
                                item.getCity(),
                                item.getName()),
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
                CheckLeadResult result = clientService.getAllFromCheckLead(Collections.singletonList(item.getInn()), null);
                //и проверям добавился лид или нет
                if (item.getBank() == Bank.VTB_OPENING) {
                    item.setStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE);
                    item.setOpeningRequestId(result.getAdditionalInfo());
                } else {
                    if (result.getLeadResponse().stream().anyMatch(lead ->
                            lead.getInn().equals(item.getInn()) && lead.getResponseCode() != CheckLeadStatus.POSITIVE)) {
                        log.debug("*** lead loaded in {} {} ", item.getBank(), item.getInn());
                        item.setStatus(WebhookStatus.SUCCESS);
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Заявка успешно добавлена",
                                        item.getInn(),
                                        item.getCity(),
                                        item.getName()),
                                EventType.LOG_TG));
                    } else {
                        item.setStatus(WebhookStatus.ERROR);
                    }
                }
            } catch (TimeOutException ex) {
                log.debug("*** FROM STATUS TRY TO CREATE LEAD TIMEOUT ERROR " + item.getInn());
                item.setStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
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
                    item.setStatus(WebhookStatus.SUCCESS);
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Заявка успешно добавлена",
                                    item.getInn(),
                                    item.getCity(),
                                    item.getName()),
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
                //ScenarioDto scenarioDto = skorozvonClientService.getScenarioById(webhookDto.getCall().getScenarioId());

                //создаем заливку в банк
                webhookSuccessStatusService.getByNameAndStatus(resultName).ifPresent(status -> {
//                    contactNewService.changeWebhookStatus(webhookDto.getLead().getInn(), status);
                    webhookStatisticService.addStatistic(WebhookStatus.FIXED, webhookDto, status);
                });

                if (webhookDto.getCall().getScenarioId() != null) {
                    enabledScenarioService.findByScenarioId(webhookDto.getCall().getScenarioId()).ifPresent(scenario ->
                            contactNewService.updateActuality(webhookDto.getLead().getInn(), scenario.getBank(), webhookStatus));
                }
            }
        }
    }

    //обработка файла загрженного с интерфейса
    public void processXlsxRecords(FileInfoEntity fileInfo) throws IOException {

        eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                EventType.FILE_PROCESS, fileInfo.getId()));
        Map<String, List<XlsxRecordDto>> records = getValidInnListFromFile(fileInfo);
        saveNewContacts(records);
        //запуск обработки по разным банкам
        if (!fileInfo.getBankList().isEmpty()) {
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
        List<String> innSkipFilter = (List<String>) downloadFilterService.getByName(DownloadFilter.INN).getFilter();
        return xlsxParserService.readColumnBody(fileInfo).stream()
                .filter(item -> isNotBlank(item.getOrgInn()))
                .filter(item -> item.getOrgInn().length() == 10 || item.getOrgInn().length() == 12)
                .filter(item -> {
                    if (innSkipFilter.isEmpty()) {
                        return true;
                    }
                    return innSkipFilter.stream().noneMatch(innPrefix -> item.getOrgInn().startsWith(innPrefix));
                })
                .collect(groupingBy(XlsxRecordDto::getOrgInn));
    }

    private void saveNewContacts(Map<String, List<XlsxRecordDto>> records) {
        List<ContactNewEntity> contacts = records.values()
                .stream()
                .map(value -> value.get(0))
                .filter(record -> !contactNewService.existsByInn(record.getOrgInn()))
                .map(record -> {
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
//                    contact.setBank(fileBank.getBank());
                    return contact;
                })
                .filter(contact -> checkLength(contact.getOrgName(), 1000)
                        && checkLength(contact.getName(), 100)
                        && checkLength(contact.getSurname(), 100)
                        && checkLength(contact.getMiddleName(), 100)
                        && checkLength(contact.getPhone(), 20)
                        && checkLength(contact.getInn(), 20)
                        && checkLength(contact.getOgrn(), 20)
                        && checkLength(contact.getRegion(), 500)
                        && checkLength(contact.getCity(), 1000))
                .collect(Collectors.toList());
        contactNewService.save(contacts);
//        return contactService.filteredContacts(contacts, fileBank);
    }

    private boolean checkLength(String value, int length) {
        return value == null || value.length() <= length;
    }
}
