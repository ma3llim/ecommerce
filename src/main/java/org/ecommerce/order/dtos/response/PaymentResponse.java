package org.ecommerce.order.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.PaymentMethod;
import org.ecommerce.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentResponse(
        @JsonAlias("id")
        UUID paymentId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
