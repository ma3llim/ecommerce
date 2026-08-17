package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.UUID;

public record ProductFaqResponse(
        UUID id,
        UUID productId,
        String question,
        String answer,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
