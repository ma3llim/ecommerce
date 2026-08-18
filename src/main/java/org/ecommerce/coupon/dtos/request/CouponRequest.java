package org.ecommerce.coupon.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CouponRequest(
        @NotBlank(message = "Coupon code is required")
        String code
) {
}
