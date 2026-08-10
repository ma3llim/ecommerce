package org.ecommerce.auth.service;

import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.enums.channel.NotificationChannel;
import org.ecommerce.common.notification.enums.channel.NotificationEvent;
import org.ecommerce.common.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceMailTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("Should send OTP verification email with correct notification details")
    void sendOtpMail_whenCalled_sendsCorrectOtpVerificationNotification() {

        String fullName = "John Doe";
        String otp = "123456";
        String expiryMinutes = "10";
        String recipientEmail = "john@example.com";

        authService.sendOtpMail(
                fullName,
                otp,
                expiryMinutes,
                recipientEmail
        );

        ArgumentCaptor<NotificationRequest> requestCaptor = ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService)
                .send(requestCaptor.capture());

        NotificationRequest request =
                requestCaptor.getValue();

        assertEquals(
                NotificationChannel.EMAIL,
                request.getChannel()
        );

        assertEquals(
                NotificationEvent.OTP_VERIFICATION,
                request.getEvent()
        );

        assertEquals(
                recipientEmail,
                request.getRecipient()
        );

        assertEquals(
                fullName,
                request.getData().get("fullName")
        );

        assertEquals(
                otp,
                request.getData().get("otp")
        );

        assertEquals(
                expiryMinutes,
                request.getData().get("expiryMinutes")
        );
    }

    @Test
    @DisplayName("Should send password reset email with correct notification details")
    void sendForgotOtpMail_whenCalled_sendsCorrectPasswordResetNotification() {

        String fullName = "John Doe";
        String otp = "654321";
        String expiryMinutes = "10";
        String recipientEmail = "john@example.com";

        authService.sendForgotOtpMail(
                fullName,
                otp,
                expiryMinutes,
                recipientEmail
        );

        ArgumentCaptor<NotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(notificationService).send(requestCaptor.capture());

        NotificationRequest request =
                requestCaptor.getValue();

        assertEquals(
                NotificationChannel.EMAIL,
                request.getChannel()
        );

        assertEquals(
                NotificationEvent.FORGET_PASSWORD_VERIFICATION,
                request.getEvent()
        );

        assertEquals(
                recipientEmail,
                request.getRecipient()
        );

        assertEquals(
                fullName,
                request.getData().get("fullName")
        );

        assertEquals(
                otp,
                request.getData().get("otp")
        );

        assertEquals(
                expiryMinutes,
                request.getData().get("expiryMinutes")
        );
    }
}
