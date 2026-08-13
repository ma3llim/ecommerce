package org.ecommerce.user.dtos.admin;

import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record AdminUserInfoResponseDto(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        AccountStatus accountStatus,
        Role role,
        boolean emailVerified,
        Instant lastLoginAt,
        Instant createdAt
) {
}
