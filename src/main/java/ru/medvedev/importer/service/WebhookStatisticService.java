package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.DailyContactStatistic;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.entity.WebhookSuccessStatusEntity;
import ru.medvedev.importer.enums.WebhookStatus;
import ru.medvedev.importer.repository.WebhookStatisticRepository;
import ru.medvedev.importer.service.telegram.xlsxcollector.TelegramPollingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class WebhookStatisticService {

    private static final String STATISTIC_MESSAGE = "*Статистика отправленных заявок* \nс %s по %s\n%s";
    private static final String STATISTIC_BANK_ITEM = "`%s`\nДобавленные: `%d`\nОтказано в добавлении: `%d`\nОшибочные заявки: `%d`";

    @Value("${telegram.xlsx-collector.scanningChatId}")
    private Long scanningChatId;

    private final WebhookStatisticRepository repository;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final TelegramPollingService telegramPollingService;

    @Scheduled(cron = "${cron.webhook-statistic}")
    public void printScheduledStatistic() {
        String message = String.format(STATISTIC_MESSAGE,
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                getCountByPrevDay());
        telegramPollingService.sendMessage(message, null, null, scanningChatId, false);
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

    public List<WebhookStatisticEntity> getByStatus(WebhookStatus status) {
        return repository.findAllByStatus(status);
    }

    public void addStatistic(WebhookStatus status, WebhookDto webhook, WebhookSuccessStatusEntity successStatus) {
        WebhookStatisticEntity entity = new WebhookStatisticEntity();
        entity.setInn(webhook.getLead().getInn());
        entity.setBank(successStatus.getBank());
        entity.setStatus(status);
        entity.setEmail(webhook.getLead().getEmails().isEmpty() ? null : webhook.getLead().getEmails().get(0));
        entity.setPhone(webhook.getLead().getPhones());
        entity.setCity(webhook.getLead().getCity().trim());
        entity.setName(webhook.getLead().getName());
        if (webhook.getContact() != null) {
            entity.setFullName(webhook.getContact().getName());
        }
        if (webhook.getCallResult() != null) {
            entity.setComment(webhook.getCallResult().getComment());
        }
        entity.setSuccessStatus(webhookSuccessStatusService.getByName(webhook.getCallResult().getResultName()));
        repository.save(entity);
    }

    public void updateStatisticStatus(String inn, WebhookStatus oldStatus, WebhookStatus newStatus) {
        repository.saveAll(repository.findAllByInnAndStatus(inn, oldStatus)
                .stream()
                .peek(item -> item.setStatus(newStatus))
                .collect(Collectors.toList()));
    }

    public void updateStatisticStatusToError(String inn, WebhookStatus oldStatus, String message) {
        repository.saveAll(repository.findAllByInnAndStatus(inn, oldStatus)
                .stream()
                .peek(item -> item.setStatus(WebhookStatus.ERROR))
                .peek(item -> item.setErrorMessage(message))
                .collect(Collectors.toList()));
    }

    public void updateStatisticStatus(Long id, WebhookStatus newStatus) {
        repository.updateStatus(id, newStatus);
    }

    public void updateStatisticStatusAndOpeningId(Long id, WebhookStatus newStatus, String requestId) {
        repository.updateStatusAndRequestId(id, newStatus, requestId);
    }
}
