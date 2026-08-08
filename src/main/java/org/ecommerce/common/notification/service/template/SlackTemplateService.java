package org.ecommerce.common.notification.service.template;

import org.ecommerce.common.notification.enums.template.SlackTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SlackTemplateService {
    public String render(
            SlackTemplate template,
            Map<String, Object> data) {
        return switch (template) {
            case USER_REGISTERED -> buildUserRegisteredMessage(data);
        };
    }

    private String buildUserRegisteredMessage(
            Map<String, Object> data
    ) {
        String userId = String.valueOf(data.get("userId"));
        String userName = String.valueOf(data.get("userName"));
        String email = String.valueOf(data.get("email"));
        String role = String.valueOf(data.get("role"));
        String registeredAt = String.valueOf(data.get("registeredAt"));
        String provider = String.valueOf(data.get("provider"));

        return """
                🛒 *E-Commerce — New User Registration*
                
                *Event:* USER_REGISTERED
                *Status:* ✅ Registration successful
                
                *User Details*
                • *User ID:* %s
                • *Name:* %s
                • *Email:* %s
                • *Role:* %s
                • *Provider:* %s
                • *Registered At:* %s
                """.formatted(
                userId,
                userName,
                email,
                role,
                provider,
                registeredAt
        );
    }
}
