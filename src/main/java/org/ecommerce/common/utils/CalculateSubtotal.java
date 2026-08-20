package org.ecommerce.common.utils;

import org.ecommerce.order.entities.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public class CalculateSubtotal {
    public static BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
        return orderItems.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
