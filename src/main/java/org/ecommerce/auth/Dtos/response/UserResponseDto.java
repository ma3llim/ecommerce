package org.ecommerce.auth.Dtos.response;

import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;

import java.util.UUID;

public record UserResponseDto(UUID id,
                              String firstName,
                              String lastName,
                              String email,
                              boolean emailVerified,
                              AccountStatus accountStatus,
                              Role role) {

}
