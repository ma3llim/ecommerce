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
    ),
    FORGET_PASSWORD_VERIFICATION(
            "email/forget-password-verification",
            "Reset Your Password"
    ),
    ORDER_PLACED(
            "email/order-placed",
            "Your Order Has Been Placed"
    ),

    PAYMENT_SUCCESS(
            "email/payment-success",
            "Payment Successful"
    ),

    PAYMENT_FAILED(
            "email/payment-failed",
            "Payment Failed"
    ),

    ORDER_DELIVERED(
            "email/order-delivered",
            "Your Order Has Been Delivered"
    ),

    ORDER_CANCELLED(
            "email/order-cancelled",
            "Your Order Has Been Cancelled"
    ),

    REFUND_COMPLETED(
            "email/refund-completed",
            "Your Refund Has Been Completed"
    );

    private final String templatePath;
    private final String subject;

    EmailTemplate(String templatePath, String subject) {
        this.templatePath = templatePath;
        this.subject = subject;
    }
}
