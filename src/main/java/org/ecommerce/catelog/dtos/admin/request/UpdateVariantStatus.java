package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.catelog.enums.VariantStatus;

public record UpdateVariantStatus(
        @NotNull
        VariantStatus status
) {
}
