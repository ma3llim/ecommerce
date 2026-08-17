package org.ecommerce.catelog.dtos.publics;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public record ProductVariantImageResponse(
        @JsonAlias("id")
        UUID productVariantImageId,
        String url,
        Integer displayOrder,
        boolean primary
) {
}
