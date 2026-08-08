package org.ecommerce.common.notification.provider.email;

public interface EmailProvider {
    void send(
            String recipient,
            String subject,
            String htmlContent
    );
}
