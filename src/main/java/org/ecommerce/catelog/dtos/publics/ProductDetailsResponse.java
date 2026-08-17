package org.ecommerce.catelog.dtos.publics;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductDetailsResponse(
        @JsonAlias("id")
        UUID productId,
        String name,
        String slug,
        String description,
        Map<String, Object> specifications,
        UUID defaultVariantId,
        List<ProductVariantResponse> variants,
        List<ProductFaqResponse> faqs
) {
}
