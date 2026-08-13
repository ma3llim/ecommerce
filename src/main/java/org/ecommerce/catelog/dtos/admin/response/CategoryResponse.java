package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedA
) {
}
