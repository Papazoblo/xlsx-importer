package ru.medvedev.importer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookLeadDto {

    private String phones;
    private String city;
    private String inn;
    private String name;
    private String comment;
    @JsonProperty("created_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy HH:mm:ss")
    private LocalDateTime createAt;
    private List<String> emails = new ArrayList<>();
}
