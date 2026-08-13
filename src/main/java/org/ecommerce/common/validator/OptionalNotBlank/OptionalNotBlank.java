package org.ecommerce.common.validator.OptionalNotBlank;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OptionalNotBlankValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalNotBlank {
    String message() default "must not be blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
