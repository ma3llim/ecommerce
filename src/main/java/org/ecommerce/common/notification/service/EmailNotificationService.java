package org.ecommerce.common.notification.service;

import org.ecommerce.common.notification.dtos.request.NotificationRequest;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {
    @Override
    public void send(NotificationRequest request) {
        
    }
}
