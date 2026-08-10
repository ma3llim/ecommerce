package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class OtpVerificationRepositoryTest {
    @Autowired
    private OtpVerificationRepository otpVerificationRepository;
    private OtpVerification otpVerification;

    @Test
    @DisplayName("Should return OTP when user ID, purpose, and status match")
    void shouldReturnOTPWhenUserIdPurposeAndStatusMatch() {
        UUID userId = UUID.randomUUID();

        otpVerification = OtpVerification.builder()
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .userId(userId)
                .otpCode("456456")
                .expiresAt(Instant.now().plusSeconds(456))
                .status(OtpStatus.PENDING)
                .build();

        otpVerificationRepository.save(otpVerification);

        Optional<OtpVerification> result =
                otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                );

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when status does not match")
    void shouldReturnEmptyWhenStatusDoesNotMatch() {
        UUID userId = UUID.randomUUID();

        otpVerification = OtpVerification.builder()
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .userId(userId)
                .otpCode("456456")
                .expiresAt(Instant.now().plusSeconds(456))
                .status(OtpStatus.PENDING)
                .build();

        otpVerificationRepository.save(otpVerification);

        Optional<OtpVerification> result =
                otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.VERIFIED
                );

        assertTrue(result.isEmpty());
    }
}
