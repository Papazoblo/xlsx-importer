package ru.medvedev.importer.entity;

import lombok.Data;
import ru.medvedev.importer.enums.WebhookStatus;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Data
public class WebhookStatisticId implements Serializable {

    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "org_name")
    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    public static WebhookStatisticId of(LocalDate createAt, WebhookStatus status) {
        WebhookStatisticId id = new WebhookStatisticId();
        id.setCreateAt(createAt);
        id.setStatus(status);
        return id;
    }
}
