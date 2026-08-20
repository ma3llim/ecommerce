package org.ecommerce.order.dtos.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.order.enums.PaymentMethod;

import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Shipping address is required")
        UUID shippingAddressId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String couponCode
) {
}
