package com.e_commerce.users;

import com.e_commerce.users.constraints.UsernameConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsernameConstraintValidatorTests {
    private UsernameConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    public void createUsernameConstraintValidator() {
        UsernameConstraintValidator validator = new UsernameConstraintValidator();

        validator.initialize(null);

        this.validator = validator;
    }

    @BeforeEach
    public void constraintViolationIsBuilt() {
        lenient().when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);
    }

    @Test
    public void validUsername() {
        assertThat(validator.isValid("__username123._", context)).isTrue();

        verify(context, times(0)).disableDefaultConstraintViolation();
        verify(context, times(0))
                .buildConstraintViolationWithTemplate(anyString());
        verify(constraintViolationBuilder, times(0)).addConstraintViolation();

    }

    private static class InvalidUsernameTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(null, "Username must be provided."),
                    Arguments.of("", "Username must be provided."),
                    Arguments.of("a".repeat(65), "Username can only contain up to 64 characters."),
                    Arguments.of("__Username123._", "Username can only contain lower case letters, numbers, dots and underscores."),
                    Arguments.of("__username@123._", "Username can only contain lower case letters, numbers, dots and underscores."),
                    Arguments.of("__username123.._", "Username cannot contain repeated dots.")
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidUsernameTestParameters.class)
    public void invalidUsername(String username, String violation) {
        assertThat(validator.isValid(username, context)).isFalse();

        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1))
                .buildConstraintViolationWithTemplate(violation);
        verify(constraintViolationBuilder, times(1))
                .addConstraintViolation();
    }
}
