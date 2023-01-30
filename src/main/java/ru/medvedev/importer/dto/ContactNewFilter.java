package ru.medvedev.importer.dto;

import lombok.Data;
import ru.medvedev.importer.enums.ContactActuality;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContactNewFilter {

    private LocalDateTime createDateFrom;
    private LocalDateTime createDateTo;
    private List<Long> vtbWebhook = new ArrayList<>();
    private List<Long> openingWebhook = new ArrayList<>();
    private List<ContactActuality> vtbActuality = new ArrayList<>();
    private List<ContactActuality> openingActuality = new ArrayList<>();
}
