package org.ecommerce.auth.service;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceForgotPasswordTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private AuthService authService;


    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {

        String email = "john@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.forgotPassword(email)
        );

        verify(otpVerificationRepository, never())
                .save(any(OtpVerification.class));

        verify(notificationService, never())
                .send(any(NotificationRequest.class));
    }


    @Test
    @DisplayName("Should generate OTP and send password reset email when user exists")
    void shouldGenerateOtpAndSendPasswordResetEmailWhenUserExists() {

        UUID userId = UUID.randomUUID();

        String email = "john@example.com";

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UUID result = authService.forgotPassword(email);

        assertEquals(userId, result);

        verify(otpVerificationRepository)
                .save(any(OtpVerification.class));

        verify(notificationService)
                .send(any(NotificationRequest.class));
    }
}
