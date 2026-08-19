package org.ecommerce.order.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.razorpay")
@Validated
public record RazorpayProperties(
        @NotBlank
        String keyId,

        @NotBlank
        String keySecret,

        @NotBlank
        String webhookSecret
) {
}
