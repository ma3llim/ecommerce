package org.ecommerce.catelog.dtos.publics;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public record ProductFaqResponse(
        @JsonAlias("id")
        UUID productFaqId,
        String question,
        String answer
) {
}
