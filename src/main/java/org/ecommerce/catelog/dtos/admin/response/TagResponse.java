package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
        UUID id,
        String name,
        String slug,
        Instant createdAt,
        Instant updatedAt
) {
}
