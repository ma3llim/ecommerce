package org.ecommerce.user.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.auth.enums.AccountStatus;

public record adminStatusRequestDto(
        @NotNull(message = "Account type is required")
        AccountStatus accountStatus
) {
}
