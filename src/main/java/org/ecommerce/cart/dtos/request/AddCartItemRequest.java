package org.ecommerce.cart.dtos.request;

import java.util.UUID;

public record AddCartItemRequest(
        UUID productVariantId,
        Integer quantity
) {
}
