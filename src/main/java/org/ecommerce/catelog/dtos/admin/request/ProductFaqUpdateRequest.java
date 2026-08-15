package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.OptionalNotBlank.OptionalNotBlank;

public record ProductFaqUpdateRequest(
        @OptionalNotBlank(message = "Question is required")
        @Size(max = 1000, message = "Question cannot exceed 1000 characters")
        String question,

        @OptionalNotBlank(message = "Answer is required")
        String answer
) {
}
