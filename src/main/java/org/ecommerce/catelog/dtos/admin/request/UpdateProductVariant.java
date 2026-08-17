package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateProductVariant(
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        Map<String, Object> attributes
) {
}
