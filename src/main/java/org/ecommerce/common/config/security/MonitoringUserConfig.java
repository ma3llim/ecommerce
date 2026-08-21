package org.ecommerce.common.config.security;

import lombok.RequiredArgsConstructor;
import org.ecommerce.common.config.properties.MonitoringProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@RequiredArgsConstructor
public class MonitoringUserConfig {
    private final MonitoringProperties monitoringProperties;

    @Bean
    public UserDetailsService monitoringUserDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails monitoringUser = User.builder()
                .username(monitoringProperties.getUsername())
                .password(passwordEncoder.encode(monitoringProperties.getPassword()))
                .roles("MONITORING")
                .build();

        return new InMemoryUserDetailsManager(monitoringUser);
    }

    @Bean
    public AuthenticationProvider monitoringAuthenticationProvider(
            InMemoryUserDetailsManager monitoringUserDetailsManager,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(monitoringUserDetailsManager);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
