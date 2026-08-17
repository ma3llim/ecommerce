package org.ecommerce.catelog.dtos.admin.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String sku,
        BigDecimal price,
        int stockQuantity,
        Map<String, Object> attributes,
        boolean active,
        List<ProductVariantImageResponse> images) {
}
