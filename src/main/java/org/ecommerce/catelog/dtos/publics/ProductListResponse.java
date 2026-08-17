package org.ecommerce.catelog.dtos.publics;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductListResponse(
        @JsonAlias("id")
        UUID productId,
        String name,
        String description,
        String slug,
        BigDecimal price,
        String image
) {
}
