package ru.medvedev.importer.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "skorozvon")
public class SkorozvonProperties {

    private String applicationKey;
    private String apiKey;
    private String applicationId;
    private String login;
}
