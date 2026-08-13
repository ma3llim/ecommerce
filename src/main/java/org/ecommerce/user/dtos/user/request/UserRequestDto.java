package org.ecommerce.user.dtos.user.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.OptionalNotBlank.OptionalNotBlank;
import org.ecommerce.common.validator.ValidName.ValidName;

public record UserRequestDto(
        @OptionalNotBlank(message = "First name cannot be blank")
        @ValidName
        @Size(max = 30, message = "First name must not exceed 30 characters")
        String firstName,

        @OptionalNotBlank(message = "Last name cannot be blank")
        @ValidName
        @Size(max = 30, message = "Last name must not exceed 30 characters")
        String lastName,

        @Pattern(
                regexp = "^\\+?[1-9]\\d{7,14}$",
                message = "Invalid phone number"
        )
        String phoneNumber
) {
}