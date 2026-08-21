package org.ecommerce.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.monitoring")
public class MonitoringProperties {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
