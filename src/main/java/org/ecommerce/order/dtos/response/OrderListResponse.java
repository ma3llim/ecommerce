package org.ecommerce.order.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderListResponse(
        @JsonAlias("id")
        UUID orderId,
        String orderNumber,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        UUID couponId,
        String couponCode,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        Instant createdAt
) {
}
