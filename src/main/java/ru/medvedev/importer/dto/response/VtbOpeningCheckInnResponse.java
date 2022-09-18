package ru.medvedev.importer.dto.response;

import lombok.Data;
import ru.medvedev.importer.enums.VtbOpeningRequestStatus;

@Data
public class VtbOpeningCheckInnResponse {

    private String id;
    private VtbOpeningRequestStatus status;
    private VtbOpeningCheckInnResult result;
    private String message;
}
