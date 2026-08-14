package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.OptionalNotBlank.OptionalNotBlank;

import java.util.Map;
import java.util.UUID;

public record UpdateProduct(
        UUID categoryId,

        @OptionalNotBlank
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @OptionalNotBlank
        String description,

        Map<String, Object> specifications
) {
}
