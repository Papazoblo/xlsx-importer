package ru.medvedev.importer.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "telegram.xlsx-collector")
public class TelegramXlsxCollectorProperty {

    private String token;
    private String botName;
    private Long reconnectTimer;
}
