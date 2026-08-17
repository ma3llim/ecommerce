package org.ecommerce.catelog.dtos.publics;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductVariantResponse(
        @JsonAlias("id")
        UUID productVariantId,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Map<String, Object> attributes,
        List<ProductVariantImageResponse> images
) {
}
