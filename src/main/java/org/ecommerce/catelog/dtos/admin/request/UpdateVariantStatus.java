package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.common.enums.VisibleStatus;

public record UpdateVariantStatus(
        @NotNull
        VisibleStatus status
) {
}
