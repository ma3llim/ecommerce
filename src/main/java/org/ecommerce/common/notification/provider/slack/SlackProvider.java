package org.ecommerce.common.notification.provider.slack;

public interface SlackProvider {
    void send(String message);
}
