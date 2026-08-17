package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.UUID;

public record ProductTagMappingResponse(
        UUID id,
        UUID productId,
        UUID tagId,
        Instant createdAt
) {
}
