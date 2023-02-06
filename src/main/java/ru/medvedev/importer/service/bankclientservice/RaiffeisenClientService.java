package ru.medvedev.importer.service.bankclientservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.client.RaiffeisenDictionaryApiClient;
import ru.medvedev.importer.client.RaiffeisenRequestApiClient;
import ru.medvedev.importer.client.VtbApiClient;
import ru.medvedev.importer.component.VtbProperties;
import ru.medvedev.importer.dto.*;
import ru.medvedev.importer.dto.raiffeisen.*;
import ru.medvedev.importer.dto.request.LeadRequest;
import ru.medvedev.importer.dto.response.CheckLeadBadRequestResponse;
import ru.medvedev.importer.dto.response.CheckLeadResponse;
import ru.medvedev.importer.entity.CityEntity;
import ru.medvedev.importer.exception.ErrorCheckLeadException;
import ru.medvedev.importer.exception.ErrorCreateVtbLeadException;
import ru.medvedev.importer.exception.TimeOutException;
import ru.medvedev.importer.service.CityService;
import ru.medvedev.importer.service.ContactNewService;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RaiffeisenClientService implements BankClientService {

    private final RaiffeisenDictionaryApiClient dictionaryApiClient;
    private final RaiffeisenRequestApiClient raiffeisenRequestApiClient;
    private final CityService cityService;
    private final ContactNewService contactService;


    public CreateLeadResult createLead(WebhookLeadDto webhookLead) {
        //todo добавить метаинформацию
        RaiffeisenRequestCreateLeadDto request = new RaiffeisenRequestCreateLeadDto();
        RaiffeisenCommunicationChannelsDto channelsDto = new RaiffeisenCommunicationChannelsDto();
        RaiffeisenPhoneDto phoneDto = new RaiffeisenPhoneDto();
        phoneDto.setCountryCode(webhookLead.getPhones().substring(0, 2));
        phoneDto.setPhoneNumber(webhookLead.getPhones().substring(2));
        channelsDto.setPhone(phoneDto);
        RaiffeisenPersonDto personDto = new RaiffeisenPersonDto();
        String[] personName = webhookLead.getName().split(" ");
        personDto.setFirstName(personName[1]);
        personDto.setLastName(personName[0]);
        if (personName.length > 2) {
            personDto.setMiddleName(personName[2]);
        }
        RaiffeisenCreateLeadInfoDto leadInfo = new RaiffeisenCreateLeadInfoDto();
        Optional<CityEntity> cityOptional = cityService.getByName(webhookLead.getCity());
        if (cityOptional.isPresent()) {
            leadInfo.setCity(cityOptional.get().getRaiffeisenId());
        } else {
            return CreateLeadResult.of(false, String.format("Город %s отсутствует в справочнике", webhookLead.getCity()));
        }
        leadInfo.setCity(webhookLead.getCity());
        leadInfo.setPersonName(personDto);
        leadInfo.setInn(webhookLead.getInn());
        leadInfo.setCommunicationChannels(channelsDto);
        contactService.getByInn(webhookLead.getInn()).ifPresent(contact ->
                leadInfo.setCompanyName(contact.getOrgName()));

        ResponseEntity<Object> response = raiffeisenRequestApiClient.createRequest(request);
        if (response.getStatusCode().is2xxSuccessful()) {
            return CreateLeadResult.of(true);
        } else {
            //todo добавить вывод ошибок
            return CreateLeadResult.of(false);
        }
    }

    @Override
    @Scheduled(cron = "${cron.raiffeisen-city-dictionary}")
    public void getCityDictionary() {
        ResponseEntity<RaiffeisenCityResponseDto> cityResponse = dictionaryApiClient.getCitiesList();
        if (cityResponse.getStatusCode().is2xxSuccessful()) {
            cityService.refreshList(cityResponse.getBody().getData().stream()
                    .map(item -> {
                        CityDto cityDto = new CityDto();
                        cityDto.setName(item.getText());
                        cityDto.setRaiffeisenId(item.getHid());
                        return cityDto;
                    })
                    .collect(Collectors.toList()));
        }
    }
}
