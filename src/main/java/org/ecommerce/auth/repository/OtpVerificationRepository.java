package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    Optional<OtpVerification> findByUserIdAndPurposeAndStatus(
            UUID userId,
            OtpPurpose purpose,
            OtpStatus status
    );
}
