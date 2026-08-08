package org.ecommerce.common.notification.dtos.request;

import lombok.Builder;
import lombok.Getter;
import org.ecommerce.common.notification.enums.NotificationChannel;
import org.ecommerce.common.notification.enums.NotificationEvent;

import java.util.Map;

@Getter
@Builder
public class NotificationRequest {
    private NotificationChannel channel;
    private NotificationEvent event;
    private String recipient;
    private Map<String, Object> data;
}
