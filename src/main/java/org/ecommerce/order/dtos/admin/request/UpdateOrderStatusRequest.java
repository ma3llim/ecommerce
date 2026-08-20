package org.ecommerce.order.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.order.enums.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull
        OrderStatus status

) {
}