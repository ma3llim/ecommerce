package org.ecommerce.common.validator.ValidName;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "must contain only letters and spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
