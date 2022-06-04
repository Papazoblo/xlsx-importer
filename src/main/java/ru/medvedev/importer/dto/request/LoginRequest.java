package ru.medvedev.importer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginRequest {

    @JsonProperty("grant_type")
    private String grantType = "password";

    private String username;

    @JsonProperty("client_id")
    private String applicationId;

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("client_secret")
    private String applicationKey;
}
