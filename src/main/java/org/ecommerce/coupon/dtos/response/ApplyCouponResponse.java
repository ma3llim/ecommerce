package org.ecommerce.coupon.dtos.response;

import java.math.BigDecimal;

public record ApplyCouponResponse(
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String couponCode
) {
}
