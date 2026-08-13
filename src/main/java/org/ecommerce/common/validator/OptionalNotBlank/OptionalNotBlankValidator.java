package org.ecommerce.common.validator.OptionalNotBlank;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalNotBlankValidator implements ConstraintValidator<OptionalNotBlank, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return true;

        return !value.trim().isEmpty();
    }
}
