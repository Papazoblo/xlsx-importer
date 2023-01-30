package ru.medvedev.importer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.medvedev.importer.enums.WebhookType;
import ru.medvedev.importer.service.WebhookStatusService;
import ru.medvedev.importer.service.WebhookSuccessStatusService;

@Controller
@RequestMapping("/settings/webhook-create-request-statuses")
public class WebhookCreateRequestStatusController extends BaseWebhookStatusController {

    public WebhookCreateRequestStatusController(WebhookSuccessStatusService service,
                                                WebhookStatusService webhookStatusService) {
        super(webhookStatusService, service);
    }

    @Override
    protected WebhookType getWebhookType() {
        return WebhookType.CREATE_REQUEST;
    }

    @Override
    protected String getTemplateName() {
        return "webhook_create_request_status";
    }

    @Override
    protected String getAllPage() {
        return "redirect:/settings/webhook-create-request-statuses";
    }
}
