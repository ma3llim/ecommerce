package org.ecommerce.common.notification.provider.slack;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlackWebhookProvider implements SlackProvider {
    private final RestClient restClient;

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    @Override
    public void send(String message) {
        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", message))
                .retrieve()
                .toBodilessEntity();
    }
}
