package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.events.NotificationEvent;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.entity.ContactEntity;
import ru.medvedev.importer.entity.FileInfoBankEntity;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.*;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;
import ru.medvedev.importer.exception.InnListIsEmptyException;
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
import static ru.medvedev.importer.utils.StringUtils.addPhoneCountryCode;
import static ru.medvedev.importer.utils.StringUtils.getFioStringFromContact;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadWorkerService {

    private static final int BATCH_SIZE = 100;

    private final XlsxParserService xlsxParserService;
    private final BankClientServiceFactory bankClientServiceFactory;
    private final SkorozvonAuthClientService skorozvonAuthClientService;
    private final SkorozvonClientService skorozvonClientService;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final WebhookStatisticService webhookStatisticService;
    private final ContactService contactService;
    private final ApplicationEventPublisher eventPublisher;
    private final DownloadFilterService downloadFilterService;
    private final FileInfoBankService fileInfoBankService;

    @Scheduled(cron = "${cron.webhook-change-status}")
    public void fromStatusFixed() {

        webhookStatisticService.getByStatus(WebhookStatus.FIXED).forEach(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            //мы спрашиваем у банка можем ли мы добавить лид
            try {
                CheckLeadResult result = clientService.getAllFromCheckLead(Collections.singletonList(item.getInn()), null);
                if (item.getBank() == Bank.VTB_OPENING) {
                    webhookStatisticService.updateStatisticStatusAndOpeningId(item.getId(),
                            WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE, result.getAdditionalInfo());
                } else {
                    if (result.getLeadResponse().stream().anyMatch(lead ->
                            lead.getResponseCode() == CheckLeadStatus.POSITIVE)) {
                        log.debug("*** have a positive lead inn {} ", item.getInn());
                        webhookStatisticService.updateStatisticStatus(item.getInn(), WebhookStatus.FIXED,
                                WebhookStatus.TRY_TO_CREATE_LEAD);
                    } else {
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                        String.format("Заявка отклонена (%s)", result.getLeadResponse().get(0)
                                                .getResponseCode()),
                                        item.getInn(),
                                        item.getCity(),
                                        item.getName()),
                                EventType.LOG_TG));
                        webhookStatisticService.updateStatisticStatus(item.getInn(), WebhookStatus.FIXED, WebhookStatus.REJECTED);
                    }
                }
            } catch (ErrorCheckLeadException ex) {

                log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
                eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов ВТБ. " +
                        ex.getMessage(),
                        EventType.LOG, null));
                webhookStatisticService.updateStatisticStatusToError(item.getInn(), WebhookStatus.FIXED, ex.getMessage());
            }
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitCheckLeadAfterFixedResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_FIXED_RESPONSE).forEach(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            try {
                CheckLeadResult result = clientService.getCheckLeadResult(item.getOpeningRequestId(), null);
                if (result.getStatus()) {
                    if (result.getLeadResponse().stream().anyMatch(lead ->
                            lead.getResponseCode() == CheckLeadStatus.POSITIVE)) {
                        log.debug("*** have a positive lead inn {} ", item.getInn());
                        webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.TRY_TO_CREATE_LEAD);
                    } else {
                        eventPublisher.publishEvent(new NotificationEvent(this,
                                String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                        String.format("Заявка отклонена (%s)", result.getLeadResponse().get(0)
                                                .getResponseCode()),
                                        item.getInn(),
                                        item.getCity(),
                                        item.getName()),
                                EventType.LOG_TG));
                        webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.REJECTED);
                    }
                }
            } catch (OperationNotSupportedException ex) {
                webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.ERROR);
            } catch (ErrorCheckLeadException ex) {

                log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                String.format("Ошибка обработки заявки (%s)", ex.getMessage()),
                                item.getInn(),
                                item.getCity(),
                                item.getName()),
                        EventType.LOG_TG));
                webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.ERROR);
            }
        });
    }

    @Scheduled(cron = "${cron.webhook-change-status}")
    public void fromStatusTryToCreateLead() {

        webhookStatisticService.getByStatus(WebhookStatus.TRY_TO_CREATE_LEAD).forEach(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            WebhookLeadDto leadDto = new WebhookLeadDto();
            leadDto.setCity(item.getCity());
            leadDto.setInn(item.getInn());
            leadDto.setPhones(item.getPhone());
            leadDto.setEmails(isNotBlank(item.getEmail()) ? Collections.singletonList(item.getEmail()) : Collections.emptyList());
            Optional<ContactEntity> contactEntity = contactService.findLastByInn(item.getInn());
            if (contactEntity.isPresent()) {
                leadDto.setName(getFioStringFromContact(contactEntity.get()));
            } else {
                leadDto.setName(isBlank(item.getFullName()) ? item.getName() : item.getFullName());
            }
            try {
                if (item.getBank() == Bank.VTB_OPENING) {
                    leadDto.setComment(item.getComment());
                    webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.TRY_TO_CREATE_LEAD_CHECK);
                }
                CreateLeadResult result = clientService.createLead(leadDto);
                if (result.getStatus()) {
                    if (item.getBank() == Bank.VTB_OPENING) {
                        webhookStatisticService.updateStatisticStatusAndOpeningId(item.getId(),
                                WebhookStatus.WAIT_CREATE_LEAD_RESPONSE, result.getAdditionalInfo());
                    } else {
                        webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                    }
                    return;
                } else {
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Непредвиденная ошибка", leadDto.getInn(), leadDto.getCity(), leadDto.getName()),
                            EventType.LOG_TG));
                }
            } catch (ErrorCreateVtbLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить. " + ex.getMessage(), leadDto.getInn(), leadDto.getCity(), leadDto.getName()),
                        EventType.LOG_TG));
            }
            webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.ERROR);
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitToCreateLeadResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CREATE_LEAD_RESPONSE).forEach(item -> {
            try {
                BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
                if (clientService.getCreateLeadResult(item.getOpeningRequestId())) {
                    webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS);
                }
                return;
            } catch (OperationNotSupportedException ex) {
                log.debug("*** Operation not supported exception {}", item);
            } catch (ErrorCreateVtbLeadException ex) {
                eventPublisher.publishEvent(new NotificationEvent(this,
                        String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(),
                                "Невозможно добавить. " + ex.getMessage(),
                                item.getInn(),
                                item.getCity(),
                                item.getName()),
                        EventType.LOG_TG));
            }
            webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.ERROR);
        });
    }

    @Scheduled(cron = "${cron.webhook-change-status}")
    public void fromStatusTryToCreateLeadSuccess() {

        webhookStatisticService.getByStatus(WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS).forEach(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());
            CheckLeadResult result = clientService.getAllFromCheckLead(Collections.singletonList(item.getInn()), null);
            //и проверям добавился лид или нет
            if (item.getBank() == Bank.VTB_OPENING) {
                webhookStatisticService.updateStatisticStatusAndOpeningId(item.getId(),
                        WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE, result.getAdditionalInfo());
            } else {
                if (result.getLeadResponse().stream().anyMatch(lead ->
                        lead.getInn().equals(item.getInn()) && lead.getResponseCode() != CheckLeadStatus.POSITIVE)) {
                    log.debug("*** lead loaded in {} {} ", item.getBank(), item.getInn());
                    webhookStatisticService.updateStatisticStatus(item.getInn(), WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS, WebhookStatus.SUCCESS);
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Заявка успешно добавлена",
                                    item.getInn(),
                                    item.getCity(),
                                    item.getName()),
                            EventType.LOG_TG));
                } else {
                    webhookStatisticService.updateStatisticStatus(item.getInn(), WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS, WebhookStatus.ERROR);
                }
            }
        });
    }

    @Scheduled(cron = "${cron.webhook-check-status}")
    public void fromStatusWaitCheckLeadAfterCreateResponse() {

        webhookStatisticService.getByStatus(WebhookStatus.WAIT_CHECK_LEAD_AFTER_CREATE_RESPONSE).forEach(item -> {
            BankClientService clientService = bankClientServiceFactory.getBankClientService(item.getBank());

            try {
                CheckLeadResult result = clientService.getCheckLeadResult(item.getOpeningRequestId(), null);
                if (result.getLeadResponse().stream().anyMatch(lead ->
                        lead.getInn().equals(item.getInn()) && lead.getResponseCode() != CheckLeadStatus.POSITIVE)) {
                    log.debug("*** lead loaded in {} {} ", item.getBank(), item.getInn());
                    webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.SUCCESS);
                    eventPublisher.publishEvent(new NotificationEvent(this,
                            String.format(NOTIFICATION_PATTERN, item.getBank().getTitle(), "Заявка успешно добавлена",
                                    item.getInn(),
                                    item.getCity(),
                                    item.getName()),
                            EventType.LOG_TG));
                } else {
                    webhookStatisticService.updateStatisticStatus(item.getInn(), WebhookStatus.TRY_TO_CREATE_LEAD_SUCCESS, WebhookStatus.ERROR);
                }
            } catch (OperationNotSupportedException e) {
                webhookStatisticService.updateStatisticStatus(item.getId(), WebhookStatus.ERROR);
            }
        });
    }

    public void processWebhook(WebhookDto webhookDto) {
        //если вебхук имеет тип call_result
        if (webhookDto.getType().equals("call_result")) {
            String resultName = webhookDto.getCallResult().getResultName();
            //если результат соответствует нашим ожиданиям
            if (isNotBlank(resultName)) {
                WebhookSuccessStatusEntity successStatus = webhookSuccessStatusService.getByName(resultName);
                if (successStatus != null) {
                    contactService.changeWebhookStatus(webhookDto.getLead().getInn(), successStatus);
                    webhookStatisticService.addStatistic(WebhookStatus.FIXED, webhookDto, successStatus);
                }
            }
        }
    }

    //обработка файла загрженного с интерфейса
    public void processXlsxRecords(FileInfoEntity fileInfo) throws IOException {

        eventPublisher.publishEvent(new ImportEvent(this, "Читаю файл и разбиваю контакты по банкам",
                EventType.LOG_TG, fileInfo.getId()));
        Map<String, List<XlsxRecordDto>> records = getValidInnListFromFile(fileInfo);
        //запуск обработки по разным банкам
        fileInfo.getBankList().forEach(fileBank -> {
            saveContacts(records, fileBank);
            fileInfoBankService.updateDownloadStatus(IN_QUEUE, fileBank.getId());
        });
    }

    /*public void processXlsxRecords(FileInfoEntity fileInfo) throws IOException {

        Map<Bank, Integer> statisticMap = new HashMap<>();
        Map<String, List<XlsxRecordDto>> records = getValidInnListFromFile(fileInfo);
        //запуск обработки по разным банкам
        fileInfo.getBankList().forEach(fileBank -> {
            eventPublisher.publishEvent(new ImportEvent(this, "Началась загрузка в `" + fileBank.getBank().getTitle() + "`",
                    EventType.LOG_TG, fileInfo.getId()));
            List<ContactEntity> contacts = saveContacts(records, fileBank);
            List<String> positiveInn = checkDuplicatesInBank(new ArrayList<>(records.keySet()), fileBank);
            statisticMap.put(fileBank.getBank(), positiveInn.size());
            markAsRejectedVtbContact(contacts, positiveInn);
            splitToContactAndOrganization(records, positiveInn, fileBank);
            eventPublisher.publishEvent(new ImportEvent(this, "Завершилась загрузка в `" + fileBank.getBank().getTitle() + "`",
                    EventType.LOG_TG, fileInfo.getId()));
        });
        eventPublisher.publishEvent(new ImportEvent(this, "Импорт завершён\n" +
                statisticMap.entrySet().stream()
                        .map(entry -> entry.getKey().getTitle() + ": " + entry.getValue())
                        .collect(Collectors.joining("\n")),
                EventType.SUCCESS, fileInfo.getId()));
    }*/

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

    private List<ContactEntity> saveContacts(Map<String, List<XlsxRecordDto>> records, FileInfoBankEntity fileBank) {
        List<ContactEntity> contacts = records.values()
                .stream()
                .flatMap(Collection::stream)
                .map(record -> {
                    String[] fioSplit = record.getFio().split(" ");
                    ContactEntity contact = new ContactEntity();
                    contact.setCity(isNotBlank(record.getCity()) ? record.getCity() : record.getOrgCity());
                    contact.setOrgName(record.getOrgName());
                    contact.setInn(record.getOrgInn());
                    contact.setRegion(isNotBlank(record.getRegion()) ? record.getRegion() : record.getOrgRegion());
                    contact.setName(fioSplit.length >= 2 ? fioSplit[1] : null);
                    contact.setSurname(fioSplit.length >= 1 ? fioSplit[0] : null);
                    contact.setMiddleName(fioSplit.length >= 3 ? fioSplit[2] : null);
                    contact.setOgrn(record.getOrgKpp());
                    contact.setPhone(isNotBlank(record.getPhone()) ? record.getPhone() : record.getOrgPhone());
                    contact.setBank(fileBank.getBank());

                   /* if (isBlank(contact.getOrgName())) {
                        contact.setOrgName(getFioStringFromContact(contact));
                    }*/
                    return contact;
                }).collect(Collectors.toList());
        return contactService.filteredContacts(contacts, fileBank);
    }

    private void markAsRejectedVtbContact(List<ContactEntity> contacts, List<String> positiveInn) {
        List<ContactEntity> positiveContact = new ArrayList<>();
        List<ContactEntity> negativeContact = new ArrayList<>();

        contacts.forEach(contact -> {
            if (positiveInn.stream().anyMatch(inn -> inn.equals(contact.getInn()))) {
                positiveContact.add(contact);
            } else {
                negativeContact.add(contact);
            }
        });
        contactService.changeContactStatus(positiveContact, ContactStatus.DOWNLOADED);
        contactService.changeContactStatus(negativeContact, ContactStatus.REJECTED);
    }

    private void splitToContactAndOrganization(Map<String, List<XlsxRecordDto>> recordsMap,
                                               List<String> inn, FileInfoBankEntity fileBank) {
        List<CreateOrganizationDto> orgList = createOrganizationFromInn(fileBank, recordsMap, inn);
        sendOrganizationToSkorozvon(fileBank.getProjectId(), fileBank.getFileInfo().getOrgTags(), orgList);
    }

    private void sendOrganizationToSkorozvon(Long projectId, List<String> tags, List<CreateOrganizationDto> leads) {

        log.debug("*** Send organization to skorozvon {}", leads.size());

        if (leads.isEmpty()) {
            return;
        }
        skorozvonAuthClientService.refreshToken();
        for (int i = 0; i < leads.size(); i = i + BATCH_SIZE) {
            skorozvonClientService.createMultiple(projectId, leads.subList(i, Math.min(i + BATCH_SIZE, leads.size())),
                    tags);
        }
    }

    private List<CreateOrganizationDto> createOrganizationFromInn(FileInfoBankEntity bankEntity,
                                                                  Map<String, List<XlsxRecordDto>> records,
                                                                  List<String> innList) {

        Map<String, CreateOrganizationDto> organizationMap = new HashMap<>();

        innList.forEach(item -> records.get(item).forEach(record -> {
            organizationMap.putIfAbsent(record.getOrgInn(),
                    xlsxRecordToOrganization(bankEntity, record));
            List<CreateLeadDto> leads = organizationMap.get(record.getOrgInn()).getLeads();
            if (leads.isEmpty()) {
                leads.add(xlsxRecordToLead(record));
            } else {
                boolean isFind = false;
                for (int i = 0; i < leads.size(); i++) {
                    if (leads.get(i).getName().equals(record.getFio())) {
                        List<String> phones = new ArrayList<>(leads.get(i).getPhones());
                        phones.add(addPhoneCountryCode(record.getPhone()));
                        leads.get(i).setPhones(phones);
                        isFind = true;
                        break;
                    }
                }
                if (!isFind) {
                    leads.add(xlsxRecordToLead(record));
                }
            }
            organizationMap.get(record.getOrgInn()).setLeads(leads);
        }));
        return new ArrayList<>(organizationMap.values());
    }

    private List<String> checkDuplicatesInBank(List<String> innList, FileInfoBankEntity fileBank) {
        if (innList.isEmpty()) {
            throw new InnListIsEmptyException("Список ИНН пуст", fileBank.getFileInfoId());
        }
        BankClientService bankClientService = bankClientServiceFactory.getBankClientService(fileBank.getBank());
        //Делим на пачки
        Set<String> result = new HashSet<>();
        for (int i = 0; i < innList.size(); i = i + BATCH_SIZE) {
            List<String> innSublist = innList.subList(i, Math.min(i + BATCH_SIZE, innList.size()));
            result.addAll(bankClientService.getPositiveFromCheckLead(innSublist, fileBank.getFileInfoId())
                    .getLeadResponse()
                    .stream().map(LeadInfoResponse::getInn)
                    .collect(Collectors.toSet()));
        }
        return new ArrayList<>(result);
    }

    private static CreateLeadDto xlsxRecordToLead(XlsxRecordDto record) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(record.getFio());
        lead.setPhones(Optional.ofNullable(record.getPhone())
                .map(phones -> addPhoneCountryCode(Collections.singletonList(phones))).orElse(null));
        lead.setEmails(Optional.ofNullable(record.getEmail()).map(Arrays::asList).orElse(null));
        lead.setCity(record.getCity());
        lead.setAddress(record.getAddress());
        lead.setRegion(record.getRegion());
        lead.setPost(record.getPosition());
        lead.setComment(record.getDescription());
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(FileInfoBankEntity bankEntity, XlsxRecordDto recordDto) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        organization.setName(String.format("%s %s", bankEntity.getBank().getTitle(), recordDto.getOrgName()));
        organization.setPhones(Optional.ofNullable(recordDto.getPhone())
                .map(phones -> addPhoneCountryCode(Collections.singletonList(phones))).orElse(null));
        organization.setEmails(Optional.ofNullable(recordDto.getEmail()).map(Arrays::asList).orElse(null));
        organization.setHomepage(recordDto.getOrgHost());
        organization.setCity(recordDto.getOrgCity());
        organization.setAddress(recordDto.getOrgAddress());
        organization.setRegion(recordDto.getOrgRegion());
        organization.setBusiness(recordDto.getOrgActivity());
        organization.setInn(recordDto.getOrgInn());
        organization.setKpp(recordDto.getOrgKpp());
        organization.setComment(recordDto.getOrgDescription());
        return organization;
    }
}
