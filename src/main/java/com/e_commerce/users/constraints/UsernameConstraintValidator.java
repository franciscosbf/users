package com.e_commerce.users.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameConstraintValidator implements ConstraintValidator<UsernameConstraint, String> {
    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        String violation;

        if (username == null || username.isEmpty()) {
            violation = "Username must be provided.";
        }
        else if (username.length() > 64) {
            violation = "Username can only contain up to 64 characters.";
        }
        else if (!username
                .chars()
                .allMatch(c -> (Character.isLetter(c) && Character.isLowerCase(c)) ||
                        Character.isDigit(c) ||
                        c == '.' || c == '_')) {
            violation = "Username can only contain lower case letters, numbers, dots and underscores.";
        } else if (username.contains("..")) {
            violation = "Username cannot contain repeated dots.";
        } else {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(violation)
                .addConstraintViolation();

        return false;
    }
}
