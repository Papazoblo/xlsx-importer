package ru.medvedev.importer.dto.response;

import lombok.Data;

@Data
public class CheckLeadBadRequestResponse {

    private Short httpCode;
    private String httpMessage;
    private String moreInformation;
}
