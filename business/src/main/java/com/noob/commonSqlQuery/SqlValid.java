package com.noob.commonSqlQuery;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SqlConstraintValidator.class})

public @interface SqlValid {
    String message() default "not valid! warn !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
