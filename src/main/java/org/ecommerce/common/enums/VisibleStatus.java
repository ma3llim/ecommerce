package org.ecommerce.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VisibleStatus {
    ACTIVE,
    INACTIVE;

    @JsonCreator
    public static VisibleStatus fromValue(String value) {
        return VisibleStatus.valueOf(value.toUpperCase());
    }
}
