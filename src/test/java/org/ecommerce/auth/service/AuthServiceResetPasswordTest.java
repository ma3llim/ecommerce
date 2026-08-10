package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.request.ResetPasswordRequestDto;
import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.security.JwtService;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.constants.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    private PasswordUtils passwordUtils;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private AuthService authService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    private RegisterUserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new RegisterUserRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void resetPassword_whenUserDoesNotExist_throwsBadCredentialsException() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "123456",
                "newPassword123"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(otpVerificationRepository);
        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when pending password reset OTP does not exist")
    void resetPassword_whenPendingOtpDoesNotExist_throwsBadCredentialsException() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "123456",
                "newPassword123"
        );

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(otpVerificationRepository)
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                );

        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when maximum password reset OTP attempts are exceeded")
    void resetPassword_whenMaximumOtpAttemptsExceeded_throwsBadCredentialsException() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "123456",
                "newPassword123"
        );

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setUserId(userId);
        otpVerification.setPurpose(OtpPurpose.PASSWORD_RESET);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setAttemptCount(AppConstants.MAX_OTP_ATTEMPTS);
        otpVerification.setOtpCode("123456");
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        verify(passwordUtils, never())
                .encode(anyString());

        verify(refreshTokenRepository, never())
                .findAllByUserId(any());

        assertEquals(
                AppConstants.MAX_OTP_ATTEMPTS,
                otpVerification.getAttemptCount()
        );
    }

    @Test
    @DisplayName("Should mark OTP as expired and throw exception when password reset OTP is expired")
    void resetPassword_whenOtpIsExpired_marksOtpExpiredAndThrowsException() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "123456",
                "newPassword123"
        );

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setUserId(userId);
        otpVerification.setPurpose(OtpPurpose.PASSWORD_RESET);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setAttemptCount(0);
        otpVerification.setOtpCode("123456");
        otpVerification.setExpiresAt(
                Instant.now().minusSeconds(60)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

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

        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should increment OTP attempts and throw exception when password reset OTP is incorrect")
    void resetPassword_whenOtpIsIncorrect_incrementsAttemptCountAndThrowsException() {

        UUID userId = UUID.randomUUID();

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                "999999",
                "newPassword123"
        );

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setUserId(userId);
        otpVerification.setPurpose(OtpPurpose.PASSWORD_RESET);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setAttemptCount(1);
        otpVerification.setOtpCode("123456");
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals(
                2,
                otpVerification.getAttemptCount()
        );

        verify(passwordUtils, never())
                .encode(anyString());

        verify(refreshTokenRepository, never())
                .findAllByUserId(any());
    }

    @Test
    @DisplayName("Should reset password, verify OTP, and revoke all refresh tokens when OTP is correct")
    void resetPassword_whenOtpIsCorrect_resetsPasswordVerifiesOtpAndRevokesTokens() {

        UUID userId = UUID.randomUUID();

        String otp = "123456";
        String newPassword = "newPassword123";
        String encodedPassword = "encoded-password";

        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                userId,
                otp,
                newPassword
        );

        User user = new User();
        user.setId(userId);
        user.setPassword("old-password");

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setUserId(userId);
        otpVerification.setPurpose(OtpPurpose.PASSWORD_RESET);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setAttemptCount(0);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setUserId(userId);
        refreshToken1.setRevoked(false);

        RefreshToken refreshToken2 = new RefreshToken();
        refreshToken2.setUserId(userId);
        refreshToken2.setRevoked(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.PASSWORD_RESET,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        when(passwordUtils.encode(newPassword))
                .thenReturn(encodedPassword);

        when(refreshTokenRepository.findAllByUserId(userId))
                .thenReturn(List.of(
                        refreshToken1,
                        refreshToken2
                ));

        authService.resetPassword(request);

        assertEquals(
                encodedPassword,
                user.getPassword()
        );

        assertNotNull(
                user.getPasswordChangedAt()
        );

        assertEquals(
                OtpStatus.VERIFIED,
                otpVerification.getStatus()
        );

        assertTrue(
                refreshToken1.isRevoked()
        );

        assertTrue(
                refreshToken2.isRevoked()
        );

        verify(passwordUtils)
                .encode(newPassword);

        verify(refreshTokenRepository)
                .findAllByUserId(userId);
    }
}
