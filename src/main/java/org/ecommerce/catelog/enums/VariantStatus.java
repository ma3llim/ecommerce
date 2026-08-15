package org.ecommerce.catelog.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VariantStatus {
    ACTIVE,
    INACTIVE;

    @JsonCreator
    public static VariantStatus fromValue(String value) {
        return VariantStatus.valueOf(value.toUpperCase());
    }
}
