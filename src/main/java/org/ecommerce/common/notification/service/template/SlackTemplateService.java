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

        String userName = String.valueOf(data.get("userName"));
        String email = String.valueOf(data.get("email"));

        return """
                🛒 *E-Commerce Notification*
                
                *Event:* USER_REGISTERED
                
                *User:* %s
                *Email:* %s
                
                *Status:* Registration successful
                """.formatted(
                userName,
                email
        );
    }
}
