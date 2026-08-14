package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID categoryId,
        String name,
        String slug,
        String description,
        Map<String, Object> specifications,
        UUID defaultVariantId,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
