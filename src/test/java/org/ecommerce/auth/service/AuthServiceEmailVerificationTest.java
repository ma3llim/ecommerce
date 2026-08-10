package org.ecommerce.auth.service;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceEmailVerificationTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpVerificationRepository otpVerificationRepository;
    @InjectMocks
    @Spy
    private AuthService authService;

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void resendVerification_whenUserDoesNotExist_throwsResourceNotFoundException() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resendVerification(userId)
        );

        verify(userRepository).findById(userId);

        verifyNoInteractions(otpVerificationRepository);
    }

    @Test
    @DisplayName("Should throw exception when email is already verified")
    void resendVerification_whenEmailAlreadyVerified_throwsBadRequestException() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmailVerified(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.resendVerification(userId));

        verify(userRepository).findById(userId);

        verify(otpVerificationRepository, never()).save(any(OtpVerification.class));
    }

    @Test
    @DisplayName("Should expire old OTP, create new OTP, and send email when pending OTP exists")
    void resendVerification_whenPendingOtpExists_expiresOldOtpAndCreatesNewOtp() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setEmailVerified(false);

        OtpVerification existingOtp = new OtpVerification();
        existingOtp.setUserId(userId);
        existingOtp.setOtpCode("111111");
        existingOtp.setPurpose(OtpPurpose.EMAIL_VERIFICATION);
        existingOtp.setStatus(OtpStatus.PENDING);
        existingOtp.setAttemptCount(0);
        existingOtp.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(existingOtp));

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        authService.resendVerification(userId);

        // Old OTP must be expired
        assertEquals(
                OtpStatus.EXPIRED,
                existingOtp.getStatus()
        );

        verify(otpVerificationRepository)
                .save(existingOtp);

        // New OTP must also be saved
        ArgumentCaptor<OtpVerification> otpCaptor =
                ArgumentCaptor.forClass(OtpVerification.class);

        verify(otpVerificationRepository, times(2))
                .save(otpCaptor.capture());

        OtpVerification newOtp =
                otpCaptor.getAllValues().get(1);

        assertEquals(
                userId,
                newOtp.getUserId()
        );

        assertEquals(
                OtpPurpose.EMAIL_VERIFICATION,
                newOtp.getPurpose()
        );

        assertEquals(
                OtpStatus.PENDING,
                newOtp.getStatus()
        );

        assertNotNull(newOtp.getOtpCode());

        assertTrue(
                newOtp.getOtpCode().matches("\\d{6}")
        );

        assertNotNull(newOtp.getExpiresAt());

        assertTrue(
                newOtp.getExpiresAt()
                        .isAfter(Instant.now())
        );

        // New OTP must be different from old OTP
        assertNotEquals(
                existingOtp.getOtpCode(),
                newOtp.getOtpCode()
        );

        verify(authService).sendOtpMail(
                eq(user.getFullName()),
                eq(newOtp.getOtpCode()),
                eq(String.valueOf(AppConstants.OTP_TOKEN_EXPIRY_MINUTES)),
                eq(user.getEmail())
        );
    }

    @Test
    @DisplayName("Should create new OTP and send email when no pending OTP exists")
    void resendVerification_whenNoPendingOtpExists_createsNewOtpAndSendsEmail() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setEmailVerified(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.empty());

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        authService.resendVerification(userId);

        ArgumentCaptor<OtpVerification> otpCaptor =
                ArgumentCaptor.forClass(OtpVerification.class);

        verify(otpVerificationRepository)
                .save(otpCaptor.capture());

        OtpVerification newOtp =
                otpCaptor.getValue();

        assertEquals(
                userId,
                newOtp.getUserId()
        );

        assertEquals(
                OtpPurpose.EMAIL_VERIFICATION,
                newOtp.getPurpose()
        );

        assertEquals(
                OtpStatus.PENDING,
                newOtp.getStatus()
        );

        assertNotNull(newOtp.getOtpCode());

        assertTrue(
                newOtp.getOtpCode().matches("\\d{6}")
        );

        assertNotNull(newOtp.getExpiresAt());

        assertTrue(
                newOtp.getExpiresAt()
                        .isAfter(Instant.now())
        );

        verify(authService).sendOtpMail(
                eq(user.getFullName()),
                eq(newOtp.getOtpCode()),
                eq(String.valueOf(AppConstants.OTP_TOKEN_EXPIRY_MINUTES)),
                eq(user.getEmail())
        );
    }

}
