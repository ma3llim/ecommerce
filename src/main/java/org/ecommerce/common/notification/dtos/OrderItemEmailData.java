package org.ecommerce.common.notification.dtos;

import java.math.BigDecimal;

public record OrderItemEmailData(
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
