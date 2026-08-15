package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductFaqCreateRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 1000, message = "Question cannot exceed 1000 characters")
        String question,

        @NotBlank(message = "Answer is required")
        String answer
) {
}
