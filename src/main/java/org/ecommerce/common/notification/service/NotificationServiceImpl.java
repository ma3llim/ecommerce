package org.ecommerce.common.notification.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.common.exception.NotificationException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.enums.channel.NotificationChannel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final EmailNotificationService emailNotificationService;

    @Async
    @Override
    public void send(NotificationRequest request) {
        validate(request);

        if (Objects.requireNonNull(request.getChannel()) == NotificationChannel.EMAIL) {
            emailNotificationService.send(request);
        }
    }

    private void validate(NotificationRequest request) {
        if (request == null) {
            throw new NotificationException("Notification request cannot be null");
        }
        if (request.getChannel() == null) {
            throw new NotificationException("Notification channel cannot be null");
        }
        if (request.getEvent() == null) {
            throw new NotificationException("Notification event cannot be null");
        }
        if (request.getRecipient() == null || request.getRecipient().isBlank()) {
            throw new NotificationException("Notification recipient cannot be null or blank");
        }
    }
}
