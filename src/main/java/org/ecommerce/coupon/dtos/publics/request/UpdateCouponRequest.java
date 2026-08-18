package org.ecommerce.coupon.dtos.publics.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.ecommerce.common.enums.DiscountType;
import org.ecommerce.common.validator.OptionalNotBlank.OptionalNotBlank;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateCouponRequest(
        @OptionalNotBlank(message = "Coupon name is required")
        String name,

        String description,

        DiscountType discountType,

        @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
        BigDecimal discountValue,

        @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
        BigDecimal minimumOrderAmount,

        @DecimalMin(value = "0.00", message = "Maximum discount amount cannot be negative")
        BigDecimal maximumDiscountAmount,

        @Min(value = 1, message = "Usage limit must be greater than zero")
        Integer usageLimit,

        Instant validFrom,

        Instant validUntil
) {
}
