package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Tag name is required")
        @Size(max = 100, message = "Tag name must not exceed 100 characters")
        String name
) {
}
