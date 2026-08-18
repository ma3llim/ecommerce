package org.ecommerce.review.dtos.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateReviewRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Product variant ID is required")
        UUID productVariantId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @NotBlank(message = "Review title is required")
        @Size(max = 100, message = "Review title must not exceed 100 characters")
        String title,

        @NotBlank(message = "Review is required")
        @Size(min = 10, max = 2000, message = "Review must be between 10 and 2000 characters")
        String review
) {
}
