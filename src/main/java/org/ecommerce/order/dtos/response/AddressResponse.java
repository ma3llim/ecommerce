package org.ecommerce.order.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public record AddressResponse(
        @JsonAlias("id")
        UUID addressId,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
) {
}
