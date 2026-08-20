package org.ecommerce.order.dtos.admin.request;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(
        @NotBlank(message = "Courier name is required")
        String courierName
) {
}
