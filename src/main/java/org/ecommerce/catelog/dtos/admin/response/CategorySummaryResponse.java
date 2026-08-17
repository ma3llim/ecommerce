package org.ecommerce.catelog.dtos.admin.response;

import java.util.UUID;

public record CategorySummaryResponse(
        UUID id,
        String name) {
}
