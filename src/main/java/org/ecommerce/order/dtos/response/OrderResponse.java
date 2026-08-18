package org.ecommerce.order.dtos.response;

import lombok.Builder;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID id,
        String orderNumber,
        BigDecimal subtotal,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        UUID couponId,
        String couponCode,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        PaymentResponse payment
) {
}
