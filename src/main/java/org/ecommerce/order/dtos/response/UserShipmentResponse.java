package org.ecommerce.order.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.ShipmentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record UserShipmentResponse(
        @JsonAlias("id")
        UUID shipmentId,
        String courierName,
        String trackingNumber,
        ShipmentStatus shipmentStatus,
        Instant shippedAt,
        Instant deliveredAt,
        List<ShipmentTimelineResponse> timeline
) {
}
