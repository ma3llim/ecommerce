package org.ecommerce.auth.Dtos.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        String email,
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
                message = "Password must be 8-16 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character."
        )
        String password
) {
}
