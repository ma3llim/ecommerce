package org.ecommerce.order.dtos.admin.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentResponse(
        @JsonAlias("id")
        UUID shipmentId,
        UUID orderId,
        String orderNumber,
        String courierName,
        String trackingNumber,
        ShipmentStatus shipmentStatus,
        String currentLocation,
        Instant shippedAt,
        Instant deliveredAt,
        Instant createdAt,
        Instant updatedAt
) {
}
