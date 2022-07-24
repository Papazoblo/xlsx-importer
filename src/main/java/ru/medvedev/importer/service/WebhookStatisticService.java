package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.medvedev.importer.dto.WebhookDto;
import ru.medvedev.importer.entity.WebhookStatisticEntity;
import ru.medvedev.importer.enums.WebhookStatus;
import ru.medvedev.importer.repository.WebhookStatisticRepository;
import ru.medvedev.importer.service.telegram.TelegramPollingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class WebhookStatisticService {

    private static final String STATISTIC_MESSAGE = "*Статистика отправленных заявок* \nс %s по %s\nДобавленные: %d\nОтказано в добавлении: %d";

    @Value("${telegram.scanningChatId}")
    private Long scanningChatId;

    private final WebhookStatisticRepository repository;
    private final WebhookSuccessStatusService webhookSuccessStatusService;
    private final TelegramPollingService telegramPollingService;
    private final FileInfoService fileInfoService;

    @Scheduled(cron = "${cron.webhook-statistic}")
    public void printScheduledStatistic() {
        Map<WebhookStatus, List<WebhookStatisticEntity>> entities = getCountByPrevDay();
        String message = String.format(STATISTIC_MESSAGE,
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                Optional.ofNullable(entities.get(WebhookStatus.SUCCESS)).map(List::size).orElse(0),
                Optional.ofNullable(entities.get(WebhookStatus.REJECTED)).map(List::size).orElse(0));
        telegramPollingService.sendMessage(message, scanningChatId);
    }

    public Map<WebhookStatus, List<WebhookStatisticEntity>> getCountByPrevDay() {
        LocalTime time = LocalTime.of(19, 0);
        LocalDate date = LocalDate.now();
        List<WebhookStatisticEntity> statistics = repository.findByCreateAtLessThanEqualAndCreateAtGreaterThan(
                LocalDateTime.of(date, time),
                LocalDateTime.of(date.minusDays(1), time)
        );
        return statistics.stream().collect(groupingBy(WebhookStatisticEntity::getStatus));
    }

    public void addStatistic(WebhookStatus status, WebhookDto webhook) {
        WebhookStatisticEntity entity = new WebhookStatisticEntity();
        entity.setInn(webhook.getLead().getInn());
        entity.setStatus(status);
        entity.setSuccessStatus(webhookSuccessStatusService.getByName(webhook.getCallResult().getResultName()));
        repository.save(entity);
    }
}
