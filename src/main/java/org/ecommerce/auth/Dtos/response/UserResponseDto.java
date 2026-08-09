package org.ecommerce.auth.Dtos.response;

import lombok.Builder;
import lombok.Getter;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;

import java.util.UUID;

@Getter
@Builder
public class UserResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private AccountStatus accountStatus;
    private Role role;
}
