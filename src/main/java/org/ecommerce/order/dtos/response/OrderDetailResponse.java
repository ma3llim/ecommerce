package org.ecommerce.order.dtos.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderDetailResponse(
        @JsonAlias("id")
        UUID orderId,
        String orderNumber,
        BigDecimal subtotal,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        List<OrderItemResponse> items,
        PaymentResponse payment,
        AddressResponse shippingAddress,
        UserShipmentResponse userShipmentResponse
) {
}
