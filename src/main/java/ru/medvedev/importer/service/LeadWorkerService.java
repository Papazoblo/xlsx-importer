package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.enums.CheckLeadStatus;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.WebhookStatus;
import ru.medvedev.importer.exception.BadRequestException;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadWorkerService {

    private static final int BATCH_SIZE = 150;

    private final XlsxParserService xlsxParserService;
    private final VtbClientService vtbClientService;
    private final SkorozvonAuthClientService skorozvonAuthClientService;
    private final SkorozvonClientService skorozvonClientService;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final WebhookStatisticService webhookStatisticService;
    private final ContactService contactService;
    private final ApplicationEventPublisher eventPublisher;

    public void processWebhook(WebhookDto webhookDto) {
        if (webhookDto.getType().equals("call_result")) {
            String resultName = webhookDto.getCallResult().getResultName();
            contactService.changeWebhookStatus(webhookDto.getLead().getInn(), resultName);
            if (isNotBlank(resultName) && webhookSuccessStatusService.existByName(resultName)) {
                String inn = webhookDto.getLead().getInn();
                List<LeadInfoResponse> response = vtbClientService.getPositiveFromCheckLead(
                        Collections.singletonList(inn));
                if (!response.isEmpty()) {
                    log.debug("*** have a positive lead inn {} ", response.get(0).getInn());
                    vtbClientService.createLead(webhookDto.getLead());
                    response = vtbClientService.getAllFromCheckLead(Collections.singletonList(webhookDto.getLead().getInn()));
                    if (response.stream().anyMatch(item ->
                            item.getInn().equals(inn) && item.getResponseCode() != CheckLeadStatus.POSITIVE)) {
                        log.debug("*** lead loaded in VTB {} ", response.get(0).getInn());
                        webhookStatisticService.addStatistic(WebhookStatus.SUCCESS, webhookDto);
                    }
                } else {
                    webhookStatisticService.addStatistic(WebhookStatus.REJECTED, webhookDto);
                }
            }
        }
    }

    @Async
    public void processXlsxRecords(XlsxImportInfo importInfo) {
        Map<String, List<XlsxRecordDto>> records;
        try {
            eventPublisher.publishEvent(new ImportEvent(this, "Запущена обработка файла с интерфейса",
                    EventType.LOG_TG, -1L));
            //boolean withOrganization = importInfo.getFieldLinks().get(SkorozvonField.ORG_NAME) != null;
            records = xlsxParserService.readColumnBody(importInfo).stream()
                    .filter(item -> item.getOrgInn().length() == 10 || item.getOrgInn().length() == 12)
                    .collect(groupingBy(XlsxRecordDto::getOrgInn));
            List<String> positiveInn = sendCheckDuplicates(new ArrayList<>(records.keySet()));
            splitToContactAndOrganization(records, positiveInn, importInfo);
            eventPublisher.publishEvent(new ImportEvent(this, "Файл успешно импортирован\nЗагружено " + positiveInn.size() + " контактов",
                    EventType.LOG_TG, -1L));
        } catch (Exception ex) {
            log.debug("*** Error excel parsing", ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка импорта файла\n" + ex.getMessage(),
                    EventType.LOG_TG, -1L));
        }
    }

    private void splitToContactAndOrganization(Map<String, List<XlsxRecordDto>> recordsMap,
                                               List<String> inn, XlsxImportInfo importInfo) {
        List<CreateOrganizationDto> orgList = createOrganizationFromInn(recordsMap, inn);
        sendOrganizationToSkorozvon(importInfo.getProjectCode(), importInfo.getOrgTags(), orgList);
    }

    /*private void splitToContactAndOrganization(Map<String, XlsxRecordDto> recordsMap,
                                               List<String> inn, XlsxImportInfo importInfo) {
        if (withOrganizations) {
        List<CreateOrganizationDto> orgList = createOrganizationFromInn(recordsMap, inn);
        List<CreateLeadDto> leadList = createLeadFromInn(recordsMap, inn.stream()
                .filter(item -> isBlank(recordsMap.get(item).get(0).getOrgName()))
                .collect(Collectors.toList()));
        sendOrganizationToSkorozvon(importInfo.getProjectCode(), importInfo.getOrgTags(), orgList);
        sendLeadToSkorozvon(importInfo.getProjectCode(), Collections.emptyList(), leadList);
        } else {
            List<CreateLeadDto> leadList = createLeadFromInn(recordsMap, inn, importInfo.getUsrTags());
            sendLeadToSkorozvon(importInfo.getProjectCode(), Collections.emptyList(), leadList);
        }
    }

     private void sendLeadToSkorozvon(Long projectId, List<String> tags, List<CreateLeadDto> leads) {
        log.debug("*** Send leads to skorozvon {}", leads.size());

        if (leads.isEmpty()) {
            return;
        }

        for (int i = 0; i < leads.size(); i = i + BATCH_SIZE) {
            skorozvonClientService.importLeads(projectId, leads.subList(i, Math.min(i + BATCH_SIZE, leads.size())),
                    tags);
        }
    }*/

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

    private List<CreateOrganizationDto> createOrganizationFromInn(Map<String, List<XlsxRecordDto>> records,
                                                                  List<String> innList) {

        Map<String, CreateOrganizationDto> organizationMap = new HashMap<>();

        innList.forEach(item -> records.get(item).forEach(record -> {
            organizationMap.putIfAbsent(record.getOrgInn(),
                    xlsxRecordToOrganization(record));
            List<CreateLeadDto> leads = organizationMap.get(record.getOrgInn()).getLeads();
            if (leads.isEmpty()) {
                leads.add(xlsxRecordToLead(record));
            } else {
                boolean isFind = false;
                for (int i = 0; i < leads.size(); i++) {
                    if (leads.get(i).getName().equals(record.getFio())) {
                        List<String> phones = new ArrayList<>(leads.get(i).getPhones());
                        phones.add(record.getPhone());
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

    private List<CreateLeadDto> createLeadFromInn(Map<String, XlsxRecordDto> records, List<String> innList) {
        return innList.stream().filter(records::containsKey)
                .map(inn -> xlsxRecordToLead(records.get(inn))).collect(Collectors.toList());
    }

    private List<String> sendCheckDuplicates(List<String> innList) {
        if (innList.isEmpty()) {
            throw new BadRequestException("Список ИНН пуст");
        }

        //Делим на пачки
        Set<String> result = new HashSet<>();
        for (int i = 0; i < innList.size(); i = i + BATCH_SIZE) {
            List<String> innSublist = innList.subList(i, Math.min(i + BATCH_SIZE, innList.size()));
            result.addAll(vtbClientService.getPositiveFromCheckLead(innSublist).stream().map(LeadInfoResponse::getInn)
                    .collect(Collectors.toSet()));
        }
        return new ArrayList<>(result);
    }

    private static CreateLeadDto xlsxRecordToLead(XlsxRecordDto record) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(record.getFio());
        lead.setPhones(Optional.ofNullable(record.getPhone()).map(Arrays::asList).orElse(null));
        lead.setEmails(Optional.ofNullable(record.getEmail()).map(Arrays::asList).orElse(null));
        lead.setCity(record.getCity());
        lead.setAddress(record.getAddress());
        lead.setRegion(record.getRegion());
        lead.setPost(record.getPosition());
        lead.setComment(record.getDescription());
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(XlsxRecordDto recordDto) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        organization.setName(recordDto.getOrgName());
        organization.setPhones(Optional.ofNullable(recordDto.getPhone()).map(Arrays::asList).orElse(null));
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
