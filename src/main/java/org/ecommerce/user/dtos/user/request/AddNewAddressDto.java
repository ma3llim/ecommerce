package org.ecommerce.user.dtos.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.ValidName.ValidName;
import org.ecommerce.user.enums.AddressType;

public record AddNewAddressDto(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        @ValidName
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must contain 10 to 15 digits")
        String phoneNumber,

        @NotBlank(message = "Address line one is required")
        @Size(max = 255, message = "Address line one must not exceed 255 characters")
        String addressLineOne,

        @Size(max = 255, message = "Address line two must not exceed 255 characters")
        String addressLineTwo,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        @ValidName
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must not exceed 100 characters")
        @ValidName
        String state,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        @ValidName
        String country,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9 -]*$", message = "Invalid postal code format")
        String postalCode,

        @NotNull(message = "Address type is required")
        AddressType addressType,

        boolean defaultShipping,
        boolean defaultBilling
) {
}
