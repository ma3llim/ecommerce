package org.ecommerce.common.notification.service;

import org.ecommerce.common.notification.dtos.request.NotificationRequest;

public interface NotificationService {
    void send(NotificationRequest request);
}
