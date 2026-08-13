package org.ecommerce.user.dtos.admin.response;

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
        Instant createdAt
) {
}
