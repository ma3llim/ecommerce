package org.ecommerce.user.dtos.admin.response;

import lombok.Builder;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;
import org.ecommerce.user.dtos.user.response.AddressResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record AdminUserDetailsResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        boolean emailVerified,
        String profileImageUrl,
        AccountStatus accountStatus,
        Role role,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt,
        List<AddressResponseDto> addresses
) {
}
