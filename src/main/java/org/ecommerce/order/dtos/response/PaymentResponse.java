package org.ecommerce.order.dtos.response;

import lombok.Builder;
import org.ecommerce.order.enums.PaymentMethod;
import org.ecommerce.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentResponse(
        UUID id,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
