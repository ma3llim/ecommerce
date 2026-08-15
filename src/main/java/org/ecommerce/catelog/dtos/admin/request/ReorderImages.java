package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ReorderImages(
        @NotEmpty(message = "Image IDs are required")
        List<UUID> imageIds
) {
}
