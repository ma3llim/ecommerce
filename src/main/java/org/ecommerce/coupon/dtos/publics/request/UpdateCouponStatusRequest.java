package org.ecommerce.coupon.dtos.publics.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.common.enums.VisibleStatus;

public record UpdateCouponStatusRequest(
        @NotNull(message = "Active status is required")
        VisibleStatus status
) {
}
