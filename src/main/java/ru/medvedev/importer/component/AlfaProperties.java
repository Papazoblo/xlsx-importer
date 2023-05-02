package ru.medvedev.importer.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("singleton")
@Component
@Data
@ConfigurationProperties(prefix = "alfa")
public class AlfaProperties {

    private String apiKey;
}
