package org.ecommerce.coupon.dtos.publics.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.ecommerce.common.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateCouponRequest(
        @NotBlank(message = "Coupon name is required")
        String name,

        String description,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
        BigDecimal minimumOrderAmount,

        @DecimalMin(value = "0.00", message = "Maximum discount amount cannot be negative")
        BigDecimal maximumDiscountAmount,

        @Min(value = 1, message = "Usage limit must be greater than zero")
        Integer usageLimit,

        @NotNull(message = "Valid from is required")
        Instant validFrom,

        @NotNull(message = "Valid until is required")
        Instant validUntil
) {
}
