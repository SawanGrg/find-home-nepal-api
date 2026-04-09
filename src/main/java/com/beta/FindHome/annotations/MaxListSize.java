package com.beta.FindHome.annotations;

import com.beta.FindHome.annotations.validators.MaxListSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxListSizeValidator.class)
public @interface MaxListSize {
    String message() default "You can upload a maximum of {max} images";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int max() default 5; // Default maximum is 5oad parameter
}
