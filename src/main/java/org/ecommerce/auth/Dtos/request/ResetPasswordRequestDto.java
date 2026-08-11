package org.ecommerce.auth.Dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResetPasswordRequestDto(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        @Pattern(regexp = "\\d{6}", message = "OTP must contain only digits")
        String otp,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "Password must be 8-16 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        String password
) {
}


