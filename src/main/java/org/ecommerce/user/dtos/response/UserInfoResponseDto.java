package org.ecommerce.user.dtos.response;

import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.AuthProvider;
import org.ecommerce.auth.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserInfoResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String profileImageUrl,
        AccountStatus accountStatus,
        Role role,
        AuthProvider provider,
        boolean emailVerified,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
}
