package org.ecommerce.auth.Dtos.response;

import lombok.Builder;
import lombok.Getter;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;

@Getter
@Builder
public class RegisterUserResponseDto {
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private AccountStatus accountStatus;
    private Role role;
}
