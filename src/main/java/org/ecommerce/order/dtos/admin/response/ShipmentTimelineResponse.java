package org.ecommerce.order.dtos.admin.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ShipmentTimelineResponse(
        @JsonAlias("id")
        UUID eventId,
        ShipmentStatus status,
        String location,
        String description,
        Instant eventTime
) {
}