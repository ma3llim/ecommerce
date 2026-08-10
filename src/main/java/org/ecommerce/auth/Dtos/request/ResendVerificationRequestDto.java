package org.ecommerce.auth.Dtos.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResendVerificationRequestDto(@NotNull UUID userId) {
}
