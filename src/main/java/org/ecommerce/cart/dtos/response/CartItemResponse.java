package org.ecommerce.cart.dtos.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productVariantId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
