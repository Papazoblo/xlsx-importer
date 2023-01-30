package ru.medvedev.importer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.WebhookStatusService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

@Controller
@RequestMapping("/settings/webhook-success-statuses")
public class WebhookSuccessStatusController extends BaseWebhookStatusController {

    public WebhookSuccessStatusController(WebhookSuccessStatusService service,
                                          WebhookStatusService webhookStatusService) {
        super(webhookStatusService, service);
    }

    @Override
    protected WebhookType getWebhookType() {
        return WebhookType.SUCCESS;
    }

    @Override
    protected String getTemplateName() {
        return "webhook_success_status";
    }

    @Override
    protected String getAllPage() {
        return "redirect:/settings/webhook-success-statuses";
    }
}
