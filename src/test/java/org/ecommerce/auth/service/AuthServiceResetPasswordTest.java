package org.ecommerce.auth.service;

import org.ecommerce.auth.Dtos.request.ResetPasswordRequestDto;
import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.constants.AppConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceResetPasswordTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordUtils passwordUtils;

    @InjectMocks
    private AuthService authService;


    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "456456",
                "newPassword123"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(otpVerificationRepository, never())
                .findByUserIdAndPurposeAndStatus(
                        any(UUID.class),
                        any(OtpPurpose.class),
                        any(OtpStatus.class)
                );

        verify(passwordUtils, never())
                .encode(any(String.class));
    }


    @Test
    @DisplayName("Should throw exception when pending password reset OTP does not exist")
    void shouldThrowExceptionWhenPendingPasswordResetOtpDoesNotExist() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "456456",
                "newPassword123"
        );

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.PASSWORD_RESET,
                OtpStatus.PENDING
        )).thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(passwordUtils, never())
                .encode(any(String.class));
    }


    @Test
    @DisplayName("Should throw exception when maximum OTP attempts are reached")
    void shouldThrowExceptionWhenMaximumOtpAttemptsAreReached() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "456456",
                "newPassword123"
        );

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        OtpVerification otpVerification = OtpVerification.builder()
                .userId(userId)
                .otpCode("456456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .status(OtpStatus.PENDING)
                .attemptCount(AppConstants.MAX_OTP_ATTEMPTS)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.PASSWORD_RESET,
                OtpStatus.PENDING
        )).thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(passwordUtils, never())
                .encode(any(String.class));
    }


    @Test
    @DisplayName("Should mark OTP as expired and throw exception when OTP has expired")
    void shouldMarkOtpExpiredAndThrowExceptionWhenOtpHasExpired() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "456456",
                "newPassword123"
        );

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        OtpVerification otpVerification = OtpVerification.builder()
                .userId(userId)
                .otpCode("456456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .status(OtpStatus.PENDING)
                .attemptCount(0)
                .expiresAt(Instant.now().minusSeconds(10))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.PASSWORD_RESET,
                OtpStatus.PENDING
        )).thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals(
                OtpStatus.EXPIRED,
                otpVerification.getStatus()
        );

        verify(otpVerificationRepository)
                .save(otpVerification);

        verify(passwordUtils, never())
                .encode(any(String.class));
    }


    @Test
    @DisplayName("Should increase OTP attempt count and throw exception when OTP is incorrect")
    void shouldIncreaseOtpAttemptCountAndThrowExceptionWhenOtpIsIncorrect() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "111111",
                "newPassword123"
        );

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        OtpVerification otpVerification = OtpVerification.builder()
                .userId(userId)
                .otpCode("456456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .status(OtpStatus.PENDING)
                .attemptCount(1)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.PASSWORD_RESET,
                OtpStatus.PENDING
        )).thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals(
                2,
                otpVerification.getAttemptCount()
        );

        verify(passwordUtils, never())
                .encode(any(String.class));
    }


    @Test
    @DisplayName("Should reset password and revoke refresh tokens when OTP is correct")
    void shouldResetPasswordAndRevokeRefreshTokensWhenOtpIsCorrect() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "456456",
                "newPassword123"
        );

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .password("old-password")
                .build();

        OtpVerification otpVerification = OtpVerification.builder()
                .userId(userId)
                .otpCode("456456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .status(OtpStatus.PENDING)
                .attemptCount(0)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        RefreshToken refreshToken1 = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .refreshToken("refresh-token-1")
                .isRevoked(false)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        RefreshToken refreshToken2 = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .refreshToken("refresh-token-2")
                .isRevoked(false)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                userId,
                OtpPurpose.PASSWORD_RESET,
                OtpStatus.PENDING
        )).thenReturn(Optional.of(otpVerification));

        when(passwordUtils.encode("newPassword123"))
                .thenReturn("encoded-new-password");

        when(refreshTokenRepository.findAllByUserId(userId))
                .thenReturn(List.of(refreshToken1, refreshToken2));

        authService.resetPassword(request);

        assertEquals(
                "encoded-new-password",
                user.getPassword()
        );

        assertNotNull(user.getPasswordChangedAt());

        assertEquals(
                OtpStatus.VERIFIED,
                otpVerification.getStatus()
        );

        assertTrue(refreshToken1.isRevoked());
        assertTrue(refreshToken2.isRevoked());

        verify(passwordUtils)
                .encode("newPassword123");

        verify(refreshTokenRepository)
                .findAllByUserId(userId);
    }
}
