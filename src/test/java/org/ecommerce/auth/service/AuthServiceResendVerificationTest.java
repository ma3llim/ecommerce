package org.ecommerce.auth.service;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceResendVerificationTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resendVerification(userId)
        );
    }

    @Test
    @DisplayName("Should throw exception when email is already verified")
    void shouldThrowExceptionWhenEmailIsAlreadyVerified() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .emailVerified(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadRequestException.class,
                () -> authService.resendVerification(userId)
        );

        verify(otpVerificationRepository, never())
                .save(any(OtpVerification.class));

        verify(notificationService, never())
                .send(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should expire existing OTP and send new OTP when pending OTP exists")
    void shouldExpireExistingOtpAndSendNewOtpWhenPendingOtpExists() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .emailVerified(false)
                .build();

        OtpVerification currentOtp = OtpVerification.builder()
                .userId(userId)
                .otpCode("123456")
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .status(OtpStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.EMAIL_VERIFICATION,
                OtpStatus.PENDING
        )).thenReturn(Optional.of(currentOtp));

        authService.resendVerification(userId);

        assertEquals(OtpStatus.EXPIRED, currentOtp.getStatus());

        verify(otpVerificationRepository)
                .save(currentOtp);

        verify(otpVerificationRepository)
                .save(argThat(otp ->
                        otp.getUserId().equals(userId)
                                && otp.getPurpose() == OtpPurpose.EMAIL_VERIFICATION
                                && otp.getStatus() == OtpStatus.PENDING
                ));

        verify(notificationService)
                .send(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should create and send new OTP when no pending OTP exists")
    void shouldCreateAndSendNewOtpWhenPendingOtpDoesNotExist() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .emailVerified(false)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.EMAIL_VERIFICATION,
                OtpStatus.PENDING
        )).thenReturn(Optional.empty());

        authService.resendVerification(userId);

        verify(otpVerificationRepository)
                .save(argThat(otp ->
                        otp.getUserId().equals(userId)
                                && otp.getPurpose() == OtpPurpose.EMAIL_VERIFICATION
                                && otp.getStatus() == OtpStatus.PENDING
                ));

        verify(notificationService)
                .send(any(NotificationRequest.class));
    }
}
