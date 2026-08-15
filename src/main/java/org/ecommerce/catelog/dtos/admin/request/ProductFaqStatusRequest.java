package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotNull;

public record ProductFaqStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
