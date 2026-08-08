package org.ecommerce.common.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnableConfigurationProperties(JwtProperties.class)
public class JwtPropertiesTest {
    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void shouldLoadJwtProperties() {

        assertThat(jwtProperties.getSecret())
                .isNotBlank();

        assertThat(jwtProperties.getAccessTokenExpiration())
                .isGreaterThanOrEqualTo(60);

        assertThat(jwtProperties.getRefreshTokenExpiration())
                .isGreaterThanOrEqualTo(300);
    }
}
