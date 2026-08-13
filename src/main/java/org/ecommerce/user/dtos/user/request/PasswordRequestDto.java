package org.ecommerce.user.dtos.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordRequestDto(
        @NotBlank(message = "Current Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "Password must be 8-16 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        String currentPassword,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "Password must be 8-16 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        String password
) {
}