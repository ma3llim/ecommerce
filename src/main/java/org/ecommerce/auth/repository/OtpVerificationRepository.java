package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
}
