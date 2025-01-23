package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.medvedev.importer.enums.BankProduct;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class WebhookLeadDto {

    private String phones;
    private String city;
    private String inn;
    private String name;
    private String comment;
    @JsonIgnore
    private String orgName;
    @JsonIgnore
    private BankProduct product;
    @JsonProperty("created_at")
    private String createAt;
    private List<String> emails = new ArrayList<>();

    public LocalDateTime getCreateAt() {
        try {
            return new SimpleDateFormat("yyyy-MM-ddHH:mm:ss")
                    .parse(createAt.replace("T", "")).toInstant()
                    .atOffset(OffsetDateTime.now().getOffset())
                    .toLocalDateTime();
        } catch (Exception e) {
            log.debug("Error convert date: {}", createAt);
            return LocalDateTime.now();
        }
    }
}
