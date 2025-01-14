package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.DailyContactStatistic;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.WebhookStatus;
import ru.medvedev.importer.exception.BadRequestException;
import ru.medvedev.importer.repository.WebhookStatisticRepository;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static feign.Util.isBlank;
import static java.util.stream.Collectors.*;
import static ru.medvedev.importer.utils.StringUtils.getFioStringFromContact;

@Service
@RequiredArgsConstructor
public class WebhookStatisticService {

    private static final String STATISTIC_MESSAGE = "*Статистика отправленных заявок* \nс %s по %s\n%s";
    private static final String STATISTIC_BANK_ITEM = "`%s`\nДобавленные: `%d`\nОтказано в добавлении: `%d`\nОшибочные заявки: `%d`";

    @Value("${telegram.xlsx-collector.scanningChatId}")
    private Long scanningChatId;

    private final WebhookStatisticRepository repository;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final InnRegionService innRegionService;
    private final ContactNewService contactNewService;
    private final CityService cityService;
    private final TelegramPollingService telegramPollingService;

    @Scheduled(cron = "${cron.webhook-statistic}")
    public void printScheduledStatistic() {
        String message = String.format(STATISTIC_MESSAGE,
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                getCountByPrevDay());
        telegramPollingService.sendMessage(message, null, null, scanningChatId, false);

        LocalDateTime timeToDelete = LocalDateTime.now().minusDays(3);
        repository.deleteAllByCreateAtLessThanEqual(timeToDelete);
    }

    public String getCountByPrevDay() {
        LocalTime time = LocalTime.of(19, 0);
        LocalDate date = LocalDate.now();
        return repository.findByCreateAtLessThanEqualAndCreateAtGreaterThan(
                        LocalDateTime.of(date, time), LocalDateTime.of(date.minusDays(1), time))
                .stream()
                .collect(groupingBy(DailyContactStatistic::getBank))
                .entrySet()
                .stream()
                .map(bankListEntry -> {
                    Map<WebhookStatus, DailyContactStatistic> statistic = bankListEntry.getValue().stream()
                            .collect(toMap(DailyContactStatistic::getStatus, item -> item));
                    return String.format(STATISTIC_BANK_ITEM,
                            bankListEntry.getKey().getTitle(),
                            Optional.ofNullable(statistic.get(WebhookStatus.SUCCESS)).map(DailyContactStatistic::getCount).orElse(0L),
                            Optional.ofNullable(statistic.get(WebhookStatus.REJECTED)).map(DailyContactStatistic::getCount).orElse(0L),
                            Optional.ofNullable(statistic.get(WebhookStatus.ERROR)).map(DailyContactStatistic::getCount).orElse(0L));
                }).collect(joining("\n"));
    }

    public Optional<WebhookStatisticEntity> getByStatus(WebhookStatus status) {
        return repository.findFirstByStatusOrderByUpdateAtDesc(status);
    }

    public void addStatistic(WebhookStatus status, WebhookDto webhook, WebhookSuccessStatusEntity successStatus) {

        ContactNewEntity contact = contactNewService.getLastByPhones(webhook.getLead().getPhones())
                .orElseThrow(() -> new BadRequestException("Lead by phone " + webhook.getLead().getPhones() + " not found"));


        WebhookStatisticEntity entity = new WebhookStatisticEntity();
        entity.setContactId(contact.getId());
        entity.setInn(contact.getInn());
        entity.setBank(successStatus.getBank());
        entity.setStatus(status);
        entity.setEmail(webhook.getLead().getEmails().isEmpty() ? null : webhook.getLead().getEmails().get(0));
        entity.setPhone(contact.getPhone());
        entity.setCity(Optional.ofNullable(webhook.getLead().getCity())
                .map(val -> val.replaceAll("г ", "")
                        .replaceAll("г. ", ""))
                .orElse("").trim());

        if (isBlank(entity.getCity())) {
            innRegionService.getByCode(contact.getInn().substring(0, 2)).ifPresent(region -> {
                entity.setCity(region.getAdministrationCenter());
                entity.setRegionId(region.getId());
            });
        }

        cityService.getByName(entity.getCity()).ifPresent(cityEntity ->
                entity.setCityEntityId(cityEntity.getId()));

        entity.setName(contact.getOrgName());
        entity.setSkorozvonCreateAt(webhook.getLead().getCreateAt());
        entity.setFullName(getFioStringFromContact(contact));
        if (webhook.getCallResult() != null) {
            entity.setComment(webhook.getCallResult().getComment());
        }
        entity.setSuccessStatus(successStatus);
        repository.save(entity);
    }

    public void save(WebhookStatisticEntity entity) {
        repository.save(entity);
    }
}
