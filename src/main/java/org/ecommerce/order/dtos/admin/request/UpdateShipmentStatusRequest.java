package org.ecommerce.order.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.order.enums.ShipmentStatus;

public record UpdateShipmentStatusRequest(
        @NotNull(message = "Shipment status is required")
        ShipmentStatus status,
        String currentLocation,
        String description
) {
}
