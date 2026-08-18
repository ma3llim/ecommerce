package org.ecommerce.coupon.dtos.publics.response;

import org.ecommerce.common.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maximumDiscountAmount,
        Integer usageLimit,
        Integer usedCount,
        Instant validFrom,
        Instant validUntil,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
