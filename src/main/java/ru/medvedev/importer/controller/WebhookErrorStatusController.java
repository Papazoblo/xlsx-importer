package ru.medvedev.importer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.WebhookStatusService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

@Controller
@RequestMapping("/settings/webhook-error-statuses")
public class WebhookErrorStatusController extends BaseWebhookStatusController {

    public WebhookErrorStatusController(WebhookSuccessStatusService service,
                                        WebhookStatusService webhookStatusService) {
        super(webhookStatusService, service);
    }

    @Override
    protected WebhookType getWebhookType() {
        return WebhookType.ERROR;
    }

    @Override
    protected String getTemplateName() {
        return "webhook_error_status";
    }

    @Override
    protected String getAllPage() {
        return "redirect:/settings/webhook-error-statuses";
    }
}
