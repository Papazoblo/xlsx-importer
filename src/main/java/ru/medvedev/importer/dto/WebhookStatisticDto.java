package ru.medvedev.importer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.medvedev.importer.enums.WebhookStatus;

@Data
@AllArgsConstructor
public class WebhookStatisticDto {

   private WebhookStatus status;
   private Long count;

}
