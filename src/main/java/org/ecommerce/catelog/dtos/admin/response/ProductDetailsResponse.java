package org.ecommerce.catelog.dtos.admin.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductDetailsResponse(
        UUID id,
        CategorySummaryResponse category,
        String name,
        String slug,
        String description,
        Map<String, Object> specifications,
        UUID defaultVariantId,
        boolean active,
        List<ProductVariantResponse> variants,
        Instant createdAt,
        Instant updatedAt
) {
}
