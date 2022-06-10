package ru.medvedev.importer.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("singleton")
@Component
@Data
@ConfigurationProperties(prefix = "skorozvon")
public class SkorozvonProperties {

    private String applicationKey;
    private String apiKey;
    private String applicationId;
    private String login;
    private String accessToken;
    private String refreshToken;
}
