package org.ecommerce.common.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.exception.NotificationException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.enums.channel.NotificationEvent;
import org.ecommerce.common.notification.enums.template.SlackTemplate;
import org.ecommerce.common.notification.provider.slack.SlackProvider;
import org.ecommerce.common.notification.service.template.SlackTemplateService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {
    private final SlackTemplateService slackTemplateService;
    private final SlackProvider slackProvider;

    public void send(NotificationRequest request) {
        SlackTemplate template = resolveTemplate(request.getEvent());
        String message = slackTemplateService.render(
                template,
                request.getData()
        );

        try {
            slackProvider.send(message);
            log.info("Slack notification sent successfully: event={}, template={}", request.getEvent(), template);
        } catch (Exception exception) {
            log.error("Slack notification sending failed: event={}, template={}", request.getEvent(), template,
                    exception);
            throw new NotificationException("Failed to send Slack notification");
        }
    }

    private SlackTemplate resolveTemplate(NotificationEvent event) {
        return switch (event) {
            case USER_REGISTERED -> SlackTemplate.USER_REGISTERED;

            default -> throw new IllegalArgumentException("Unsupported Slack notification event: " + event);
        };
    }
}
