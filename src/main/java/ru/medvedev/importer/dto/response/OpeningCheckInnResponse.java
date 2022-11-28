package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.OpeningRequestStatus;

@Data
public class OpeningCheckInnResponse {

    private String id;
    private OpeningRequestStatus status;
    private OpeningCheckInnResult result;
    private String message;
}
