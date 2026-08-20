package org.ecommerce.common.notification.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.enums.channel.NotificationEvent;
import org.ecommerce.common.notification.enums.template.EmailTemplate;
import org.ecommerce.common.notification.provider.email.EmailProvider;
import org.ecommerce.common.notification.service.template.EmailTemplateService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private final EmailTemplateService emailTemplateService;
    private final EmailProvider emailProvider;

    public void send(NotificationRequest request) {
        EmailTemplate emailTemplate = resolveTemplate(request.getEvent());

        String htmlContent = emailTemplateService.render(
                emailTemplate,
                request.getData()
        );

        emailProvider.send(
                request.getRecipient(),
                emailTemplate.getSubject(),
                htmlContent
        );
    }

    private EmailTemplate resolveTemplate(NotificationEvent event) {
        return switch (event) {
            case USER_REGISTERED -> EmailTemplate.REGISTRATION_WELCOME;
            case OTP_VERIFICATION -> EmailTemplate.OTP_VERIFICATION;
            case FORGET_PASSWORD_VERIFICATION -> EmailTemplate.FORGET_PASSWORD_VERIFICATION;
            case ORDER_PLACED -> EmailTemplate.ORDER_PLACED;
            case PAYMENT_SUCCESS -> EmailTemplate.PAYMENT_SUCCESS;
            case PAYMENT_FAILED -> EmailTemplate.PAYMENT_FAILED;
            case ORDER_DELIVERED -> EmailTemplate.ORDER_DELIVERED;
            case ORDER_CANCELLED -> EmailTemplate.ORDER_CANCELLED;
            case REFUND_COMPLETED -> EmailTemplate.REFUND_COMPLETED;

            default -> throw new IllegalArgumentException(
                    "Unsupported email notification event: " + event
            );
        };
    }
}
