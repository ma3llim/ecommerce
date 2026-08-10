package org.ecommerce.auth.service;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.constants.AppConstants;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceForgetPasswordTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @InjectMocks
    @Spy
    private AuthService authService;

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void forgotPassword_whenUserDoesNotExist_throwsResourceNotFoundException() {

        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.forgotPassword(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(otpVerificationRepository);
    }

    @Test
    @DisplayName("Should generate and save password reset OTP and return user ID")
    void forgotPassword_whenUserExists_generatesAndSavesOtpAndReturnsUserId() {

        String email = "john@example.com";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        doNothing().when(authService)
                .sendForgotOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        UUID result = authService.forgotPassword(email);

        assertEquals(
                userId,
                result
        );

        verify(userRepository)
                .findByEmail(email);

        verify(otpVerificationRepository)
                .save(any(OtpVerification.class));

        verify(authService)
                .sendForgotOtpMail(
                        eq(user.getFullName()),
                        anyString(),
                        eq(String.valueOf(
                                AppConstants.OTP_TOKEN_EXPIRY_MINUTES
                        )),
                        eq(user.getEmail())
                );
    }


    @Test
    @DisplayName("Should save pending password reset OTP with correct details")
    void forgotPassword_whenUserExists_savesCorrectPasswordResetOtp() {

        String email = "john@example.com";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        doNothing().when(authService)
                .sendForgotOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        authService.forgotPassword(email);

        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);

        verify(otpVerificationRepository)
                .save(otpCaptor.capture());

        OtpVerification savedOtp =
                otpCaptor.getValue();

        assertEquals(
                userId,
                savedOtp.getUserId()
        );

        assertEquals(
                OtpPurpose.PASSWORD_RESET,
                savedOtp.getPurpose()
        );

        assertEquals(
                OtpStatus.PENDING,
                savedOtp.getStatus()
        );

        assertNotNull(
                savedOtp.getOtpCode()
        );

        assertTrue(
                savedOtp.getOtpCode().matches("\\d{6}")
        );

        assertNotNull(
                savedOtp.getExpiresAt()
        );

        assertTrue(
                savedOtp.getExpiresAt()
                        .isAfter(Instant.now())
        );

        verify(authService)
                .sendForgotOtpMail(
                        eq(user.getFullName()),
                        eq(savedOtp.getOtpCode()),
                        eq(String.valueOf(
                                AppConstants.OTP_TOKEN_EXPIRY_MINUTES
                        )),
                        eq(user.getEmail())
                );
    }
}
