package org.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public class NotificationException extends BusinessException {
    public NotificationException(String message) {
        super(
                message,
                HttpStatus.CONFLICT,
                "NOTIFICATION_FAILED"
        );
    }
}