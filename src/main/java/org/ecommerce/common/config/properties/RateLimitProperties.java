package org.ecommerce.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private Limit auth;
    private Limit defaults;

    @Getter
    @Setter
    public static class Limit {
        private long capacity;
        private long refillToken;
        private Duration refillDuration;
    }
}
