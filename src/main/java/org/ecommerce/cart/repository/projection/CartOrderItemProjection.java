package org.ecommerce.cart.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CartOrderItemProjection {
    UUID getCartItemId();

    UUID getProductVariantId();

    UUID getProductId();

    String getProductName();

    BigDecimal getUnitPrice();

    Integer getStockQuantity();

    Integer getQuantity();

    String getSku();
}
