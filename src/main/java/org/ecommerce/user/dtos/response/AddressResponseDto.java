package org.ecommerce.user.dtos.response;

import org.ecommerce.user.enums.AddressType;

import java.time.Instant;

public record AddressResponseDto(
        String fullName,
        String phoneNumber,
        String addressLineOne,
        String addressLineTwo,
        String city,
        String state,
        String country,
        String postalCode,
        AddressType addressType,
        boolean isDefaultShipping,
        boolean isDefaultBilling,
        Instant createdAt,
        Instant updatedAt
) {
}
