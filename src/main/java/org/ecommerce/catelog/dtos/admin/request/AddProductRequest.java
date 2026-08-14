package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record AddProductRequest(
        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Product description is required")
        String description,
        
        Map<String, Object> specifications
) {
}
