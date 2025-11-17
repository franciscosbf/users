package com.e_commerce.users.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameConstraintValidator.class)
public @interface UsernameConstraint {
    String message() default "Invalid username.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
