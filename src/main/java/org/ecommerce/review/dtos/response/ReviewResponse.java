package org.ecommerce.review.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        @JsonAlias("id")
        UUID reviewId,
        UUID productId,
        UUID productVariantId,
        Integer rating,
        String title,
        String review,
        boolean verifiedPurchase,
        Instant createdAt,
        Instant updatedAt
) {
}
