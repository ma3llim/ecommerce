package org.ecommerce.catelog.dtos.admin.response;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        @JsonAlias("id")
        UUID categoryId,
        String name,
        String slug,
        String imageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
