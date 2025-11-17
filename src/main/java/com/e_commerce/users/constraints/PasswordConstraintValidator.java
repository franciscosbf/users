package com.e_commerce.users.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.List;

public class PasswordConstraintValidator implements ConstraintValidator<PasswordConstraint, String> {
    private PasswordValidator validator;

    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {
        validator = new PasswordValidator(
                new LengthRule(8, 16),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new CharacterOccurrencesRule(3),
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 3, false),
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 3, false),
                new IllegalSequenceRule(EnglishSequenceData.USQwerty, 3, false),
                new WhitespaceRule()
        );
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        List<String> violations;

        if (password == null || password.isEmpty()) {
            violations = List.of("Password must be provided.");
        }
        else {
            RuleResult result = validator.validate(new PasswordData(password));
            if (result.isValid()) {
                return true;
            }

            violations = validator.getMessages(result);
        }

        context.disableDefaultConstraintViolation();
        for (String violation: violations) {
            context.buildConstraintViolationWithTemplate(violation)
                    .addConstraintViolation();
        }

        return false;
    }
}
