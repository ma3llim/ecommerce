package org.ecommerce.catelog.dtos.publics;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ProductReviewResponse(
        UUID id,
        Integer rating,
        String title,
        String review,
        boolean verifiedPurchase,
        Instant createdAt
) {
}
