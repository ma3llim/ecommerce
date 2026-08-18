package org.ecommerce.cart.dtos.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        BigDecimal totalAmount,
        List<CartItemResponse> items
) {
}
