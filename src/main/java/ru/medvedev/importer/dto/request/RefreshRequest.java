package ru.medvedev.importer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshRequest {

    @JsonProperty("grant_type")
    private String grantType = "refresh_token";

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("client_id")
    private String applicationId;

    @JsonProperty("client_secret")
    private String applicationKey;
}
