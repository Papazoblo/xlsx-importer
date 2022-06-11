package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.enums.CheckLeadStatus;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.exception.BadRequestException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadWorkerService {

    private static final int BATCH_SIZE = 500;

    private final XlsxParserService xlsxParserService;
    private final VtbClientService vtbClientService;
    private final SkorozvonClientService skorozvonClientService;

    public void processWebhook(WebhookDto webhookDto) {
        if (webhookDto.getType().equals("call_result")) {
            vtbClientService.createLead(webhookDto.getContact());
        }
    }

    /*
     * 1. Разделить на контакты и контакты с организацией +
     * 2. Разделить на пачки +
     * 3. Пульнуть в скорозвон +
     * 5. Нарисовать интерфес +
     * 6. Связать бэк и интерфес +
     * 7. Разобраться с тегами
     * 8. Добавить работу с токенами +
     * 9. Скедулер проверять наличие файла +
     * 10. Подключить авторизацию
     * 11. ССЛ
     * 4. Проверить
     * */

    public void processXlsxRecords(XlsxImportInfo importInfo) {
        Map<String, XlsxRecordDto> records;
        try {
            boolean withOrganization = importInfo.getFieldLinks().get(SkorozvonField.ORG_NAME) != null;
            records = xlsxParserService.readColumnBody(importInfo).stream()
                    .collect(toMap(XlsxRecordDto::getInn, item -> item));
            List<String> positiveInn = sendCheckDuplicates(new ArrayList<>(records.keySet()));
            splitToContactAndOrganization(withOrganization, records, positiveInn, importInfo);
        } catch (Exception ex) {
            log.debug("*** Error excel parsing", ex);
            throw new BadRequestException("Ошибка парсинга экселя", ex);
        }
    }

    private void splitToContactAndOrganization(boolean withOrganizations, Map<String, XlsxRecordDto> recordsMap,
                                               List<String> inn, XlsxImportInfo importInfo) {
        if (withOrganizations) {
            List<CreateOrganizationDto> orgList = createOrganizationFromInn(recordsMap, inn, importInfo.getOrgTags(),
                    importInfo.getUsrTags());
            List<CreateLeadDto> leadList = createLeadFromInn(recordsMap, inn.stream()
                    .filter(item -> isBlank(recordsMap.get(item).getOrgName()))
                    .collect(Collectors.toList()), importInfo.getUsrTags());
            sendOrganizationToSkorozvon(importInfo.getProjectCode(), Collections.emptyList(), orgList);
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
    }

    private void sendOrganizationToSkorozvon(Long projectId, List<String> tags, List<CreateOrganizationDto> leads) {

        log.debug("*** Send organization to skorozvon {}", leads.size());

        if (leads.isEmpty()) {
            return;
        }
        for (int i = 0; i < leads.size(); i = i + BATCH_SIZE) {
            skorozvonClientService.createMultiple(projectId, leads.subList(i, Math.min(i + BATCH_SIZE, leads.size())),
                    tags);
        }
    }

    private List<CreateOrganizationDto> createOrganizationFromInn(Map<String, XlsxRecordDto> records,
                                                                  List<String> innList, List<String> orgTag,
                                                                  List<String> contactTag) {

        Map<String, CreateOrganizationDto> organizationMap = new HashMap<>();

        innList.forEach(item -> {
            XlsxRecordDto record = records.get(item);
            organizationMap.putIfAbsent(record.getOrgName(), xlsxRecordToOrganization(record, orgTag));
            organizationMap.get(record.getOrgName()).getLeads().add(xlsxRecordToLead(record, contactTag));
        });
        return new ArrayList<>(organizationMap.values());
    }

    private List<CreateLeadDto> createLeadFromInn(Map<String, XlsxRecordDto> records, List<String> innList,
                                                  List<String> tags) {
        return innList.stream().filter(records::containsKey)
                .map(inn -> xlsxRecordToLead(records.get(inn), tags)).collect(Collectors.toList());
    }

    private List<String> sendCheckDuplicates(List<String> innList) throws ExecutionException, InterruptedException {
        if (innList.isEmpty()) {
            throw new BadRequestException("Список ИНН пуст");
        }
        vtbClientService.login();

        //Делим на пачки
        List<CompletableFuture<List<LeadInfoResponse>>> completableFutures = new ArrayList<>();
        for (int i = 0; i < innList.size(); i = i + BATCH_SIZE) {
            List<String> innSublist = innList.subList(i, Math.min(i + BATCH_SIZE, innList.size()));
            completableFutures.add(CompletableFuture.supplyAsync(() -> vtbClientService.checkLead(innSublist)));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<List<LeadInfoResponse>>> allCompletableFuture = allFutures.thenApply(future ->
                completableFutures.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList()));
        CompletableFuture<List<String>> completableFuture = allCompletableFuture.thenApply(result ->
                result.stream().flatMap(List::stream)
                        .filter(item -> item.getResponseCode() == CheckLeadStatus.POSITIVE)
                        .map(LeadInfoResponse::getInn).collect(Collectors.toList()));
        return completableFuture.get();
    }

    private static CreateLeadDto xlsxRecordToLead(XlsxRecordDto record, List<String> tags) {
        CreateLeadDto lead = new CreateLeadDto();
        lead.setName(record.getFio());
        lead.setPhones(Optional.ofNullable(record.getPhone()).map(Arrays::asList).orElse(null));
        lead.setEmails(Optional.ofNullable(record.getEmail()).map(Arrays::asList).orElse(null));
        lead.setCity(record.getCity());
        lead.setInn(record.getInn());
        lead.setRegion(record.getRegion());
        lead.setPost(record.getPosition());
        lead.setDescription(record.getDescription());
        lead.setTags(tags);
        return lead;
    }

    private static CreateOrganizationDto xlsxRecordToOrganization(XlsxRecordDto recordDto, List<String> tags) {
        CreateOrganizationDto organization = new CreateOrganizationDto();
        organization.setFirmName(recordDto.getOrgName());
        organization.setPhones(Optional.ofNullable(recordDto.getPhone()).map(Arrays::asList).orElse(null));
        organization.setEmails(Optional.ofNullable(recordDto.getEmail()).map(Arrays::asList).orElse(null));
        organization.setHomepage(recordDto.getOrgHost());
        organization.setCity(recordDto.getOrgCity());
        organization.setAddress(recordDto.getOrgAddress());
        organization.setRegion(recordDto.getOrgRegion());
        organization.setBusiness(recordDto.getOrgActivity());
        organization.setInn(recordDto.getInn());
        organization.setKpp(recordDto.getOrgKpp());
        organization.setDescription(recordDto.getOrgDescription());
        organization.setTags(tags);
        return organization;
    }
}
