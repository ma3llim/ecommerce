package org.ecommerce.common.notification.service.template;

import lombok.RequiredArgsConstructor;
import org.ecommerce.common.notification.enums.template.EmailTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final SpringTemplateEngine templateEngine;

    public String render(
            EmailTemplate template,
            Map<String, Object> data
    ) {
        Context context = new Context();
        if (data != null) {
            context.setVariables(data);
        }

        return templateEngine.process(
                template.getTemplatePath(),
                context
        );
    }
}