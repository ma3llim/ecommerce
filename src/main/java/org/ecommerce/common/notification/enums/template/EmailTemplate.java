package org.ecommerce.common.notification.enums.template;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    REGISTRATION_WELCOME(
            "email/registration-welcome",
            "Welcome to Our E-Commerce Store"
    ),
    OTP_VERIFICATION(
            "email/otp-verification",
            "Verify Your Email Address"
    );
    private final String templatePath;
    private final String subject;

    EmailTemplate(String templatePath, String subject) {
        this.templatePath = templatePath;
        this.subject = subject;
    }
}
