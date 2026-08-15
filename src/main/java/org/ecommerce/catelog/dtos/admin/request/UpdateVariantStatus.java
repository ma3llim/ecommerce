package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.catelog.enums.VisibleStatus;

public record UpdateVariantStatus(
        @NotNull
        VisibleStatus status
) {
}
