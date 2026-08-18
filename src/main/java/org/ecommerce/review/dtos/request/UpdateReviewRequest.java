package org.ecommerce.review.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.ValidName.ValidName;

public record UpdateReviewRequest(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @ValidName
        @Size(max = 100, message = "Review title must not exceed 100 characters")
        String title,

        @ValidName
        @Size(min = 10, max = 2000, message = "Review must be between 10 and 2000 characters")
        String review
) {
}
