package org.ecommerce.catelog.dtos.admin.response;

import java.util.UUID;

public record ProductVariantImageResponse(
        UUID id,
        String imageUrl,
        int displayOrder,
        boolean primary) {
}
