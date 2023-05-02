package ru.medvedev.importer.service.bankclientservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.AlfaApiClient;
import ru.medvedev.importer.client.OpeningApiClient;
import ru.medvedev.importer.dto.CheckLeadResult;
import ru.medvedev.importer.dto.CityDto;
import ru.medvedev.importer.dto.CreateLeadResult;
import ru.medvedev.importer.dto.WebhookLeadDto;
import ru.medvedev.importer.dto.alfa.*;
import ru.medvedev.importer.dto.events.ImportEvent;
import ru.medvedev.importer.dto.raiffeisen.RaiffeisenCityResponseDto;
import ru.medvedev.importer.dto.request.OpeningCheckInn;
import ru.medvedev.importer.dto.request.OpeningCreateLead;
import ru.medvedev.importer.dto.response.LeadInfoResponse;
import ru.medvedev.importer.dto.response.OpeningCheckInnResponse;
import ru.medvedev.importer.dto.response.OpeningCheckResultResponse;
import ru.medvedev.importer.dto.response.OpeningResponse;
import ru.medvedev.importer.entity.CityEntity;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.enums.EventType;
import ru.medvedev.importer.enums.OpeningInnStatus;
import ru.medvedev.importer.enums.OpeningRequestStatus;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateLeadException;
import ru.medvedev.importer.exception.TimeOutException;
import ru.medvedev.importer.repository.WebhookStatisticRepository;
import ru.medvedev.importer.service.BankCountSendedRequestService;
import ru.medvedev.importer.service.CityService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static feign.Util.isBlank;
import static java.util.stream.Collectors.toList;
import static ru.medvedev.importer.enums.AlfaProductStatus.*;
import static ru.medvedev.importer.enums.Bank.ALFA;
import static ru.medvedev.importer.enums.CheckLeadStatus.*;
import static ru.medvedev.importer.enums.OpeningRequestStatus.created;
import static ru.medvedev.importer.enums.OpeningRequestStatus.exported;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlfaClientService implements BankClientService {

    private final AlfaApiClient client;
    private final CityService cityService;
    private final WebhookStatisticRepository webhookStatisticRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BankCountSendedRequestService bankCountSendedRequestService;

    public CreateLeadResult createLead(WebhookLeadDto webhookLead) {

        log.debug("*** Create lead in Alfa");

        Optional<CityEntity> city = cityService.getByName(webhookLead.getCity());
        if (!city.isPresent() || isBlank(city.get().getAlfaId())) {
            log.debug("*** Alfa error create lead. Not fount city with name {}", webhookLead.getCity());
            throw new ErrorCreateLeadException("Ошибка добавления лида. Не найден город " + webhookLead.getCity(), -1L);
        }

        CreateLeadRequestDto request = new CreateLeadRequestDto();
        request.setOrganizationInfo(new OrganizationInfoDto(webhookLead.getOrgName(), webhookLead.getInn()));
        ContactInfoDto contactInfoDto = new ContactInfoDto();
        contactInfoDto.setPhoneNumber(webhookLead.getPhones().replace("+", ""));
        contactInfoDto.setContactEmail(webhookLead.getEmails().isEmpty() ? "" : webhookLead.getEmails().get(0));
        contactInfoDto.setFullName(webhookLead.getName());
        request.setContactInfo(Collections.singletonList(contactInfoDto));
        RequestInfoDto requestInfoDto = new RequestInfoDto();
        requestInfoDto.setComment(webhookLead.getComment());
        requestInfoDto.setCityCode(city.get().getAlfaId()); // код города
        request.setRequestInfo(requestInfoDto);
        request.getProductInfo().add(new ProductInfoDto());

        try {
            bankCountSendedRequestService.incCreateRequestCount(ALFA, 1);
            ResponseEntity<OpeningResponse> response = client.creteAddLeadRequest(request);
            if (response.getStatusCode() == HttpStatus.OK) {
                String id = response.getBody().getId();
                log.debug("*** createLeadRequest id = {}", id);
                return CreateLeadResult.of(true, id);
            } else {
                log.debug("*** VtbOpening error create lead [{}]", response.getStatusCode());
                return CreateLeadResult.of(false);
            }
        } catch (TimeOutException | ErrorCreateLeadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** ошибка добавления лида " + ex.getMessage(), ex);
            return CreateLeadResult.of(false);
        }
    }

    @Override
    public CheckLeadResult getAllFromCheckLead(List<String> innList, Long fileId) {

        log.debug("*** Check duplicate in Alfa");
        List<LeadInfoResponse> result = new ArrayList<>();

        try {
            if (innList.size() > 1) {
                for (int i = 0; i < innList.size(); i += ALFA.getBatchSize()) {
                    List<String> innSubList = innList.subList(i, Math.min((i + ALFA.getBatchSize()), innList.size()));
                    bankCountSendedRequestService.incCheckRequestCount(ALFA, innSubList.size());
                    CompletableFuture.allOf(innSubList.stream()
                            .map(inn -> CompletableFuture.completedFuture(inn)
                                    .thenRun(() -> {
                                        CheckLeadInnDto request = new CheckLeadInnDto();
                                        request.getProductInfo().add(new ProductInfoDto());
                                        request.setOrganizationInfo(new OrganizationInfoDto(null, inn));
                                        LeadInfoResponse info = new LeadInfoResponse();
                                        info.setInn(inn);
                                        try {
                                            client.createCheckLeadsRequest(request);
                                            info.setResponseCode(POSITIVE);
                                        } catch (ErrorCheckLeadException ex) {
                                            log.debug(ex.getMessage());
                                            info.setResponseCode(NEGATIVE);
                                        }
                                        result.add(info);
                                    }))
                            .toArray(CompletableFuture[]::new));
                }
                return CheckLeadResult.of(true, "", result);
            } else {

                CheckLeadInnDto request = new CheckLeadInnDto();
                request.getProductInfo().add(new ProductInfoDto());
                request.setOrganizationInfo(new OrganizationInfoDto(null, innList.get(0)));
                LeadInfoResponse info = new LeadInfoResponse();
                info.setInn(innList.get(0));
                try {
                    bankCountSendedRequestService.incCheckRequestCount(ALFA, 1);
                    client.createCheckLeadsRequest(request);
                    info.setResponseCode(POSITIVE);
                    return CheckLeadResult.of(true, "", Collections.singletonList(info));
                } catch (ErrorCheckLeadException ex) {
                    log.debug(ex.getMessage());
                    info.setResponseCode(NEGATIVE);
                    return CheckLeadResult.of(false, ex.getMessage(), Collections.singletonList(info));
                }
            }
        } catch (TimeOutException ex) {
            throw ex;
        } catch (Exception ex) {
            log.debug("*** Error check duplicate: {} {}", ex.getMessage(), ex);
            eventPublisher.publishEvent(new ImportEvent(this, "Ошибка проверки дубликатов Альфа. " +
                    ex.getMessage(),
                    EventType.LOG, fileId));
            return CheckLeadResult.of(false, "blank", Collections.emptyList());
        }
    }

    @Override
    public boolean getCreateLeadResult(String id) {

        bankCountSendedRequestService.incResultCreateRequestCount(ALFA, 1);
        ResponseEntity<RequestStatusResponseDto> response = client.getRequestStatus(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            RequestStatusResponseDto result = response.getBody();
            if (result.getProductInfo().isEmpty()) {
                throw new ErrorCreateLeadException(result.getStatus() + ". Отсутствует информация о продукте.", -1L);
            }

            Optional<WebhookStatisticEntity> webhookStatisticEntity = webhookStatisticRepository.findAllByBankAndOpeningRequestId(ALFA, id);
            if (webhookStatisticEntity.isPresent() && !response.getBody().getProductInfo().isEmpty()) {
                LeadInfoResponse leadInfo = new LeadInfoResponse();
                leadInfo.setInn(webhookStatisticEntity.get().getInn());
                if (Stream.of(DECLINED, UNDEFINED)
                        .anyMatch(status -> status == response.getBody().getProductInfo().get(0).getState())) {
                    throw new ErrorCreateLeadException(response.getBody().getProductInfo().get(0).getStatus() + ". Статус заявки: `" + response.getBody().getProductInfo().get(0).getState() + "`", -1L);
                } else if (response.getBody().getProductInfo().get(0).getState() == IN_PROGRESS/*COMPLETED*/) {
                    return true;
                }
            }
        }
        throw new ErrorCreateLeadException(response.getStatusCode().toString() + ". Ошибка добавления лида", -1L);
    }

    @Override
    @Scheduled(cron = "${cron.raiffeisen-city-dictionary}")
    public void getCityDictionary() {
        ResponseEntity<CityResponseDto> cityResponse = client.getCityList();
        if (cityResponse.getStatusCode().is2xxSuccessful()) {
            cityService.refreshList(ALFA, cityResponse.getBody().getValues().stream()
                    .map(item -> {
                        CityDto cityDto = new CityDto();
                        cityDto.setName(item.getName());
                        cityDto.setAlfaId(item.getFias());
                        return cityDto;
                    })
                    .collect(Collectors.toList()));
        }
    }
}
