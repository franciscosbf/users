package com.e_commerce.users;

import com.e_commerce.users.constraints.PasswordConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordConstraintValidatorTests {
    private PasswordConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder lenientConstraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    public void createPasswordConstraintValidator() {
        PasswordConstraintValidator validator = new PasswordConstraintValidator();

        validator.initialize(null);

        this.validator = validator;
    }

    @BeforeEach
    public void ensureLenientConstraintViolationBuilderIsReturned() {
        lenient().when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(lenientConstraintViolationBuilder);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "P4ssword@",
            "P4sssword@",
            "P4ssword@ab",
            "P4sssword@12",
            "P4ssword@as"
    })
    public void validPassword(String password) {
        assertThat(validator.isValid(password, context)).isTrue();

        verify(context, times(0)).disableDefaultConstraintViolation();
        verify(context, times(0))
                .buildConstraintViolationWithTemplate(anyString());
        verify(lenientConstraintViolationBuilder, times(0))
                .addConstraintViolation();
    }

    private static class InvalidPasswordTestParameters implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(null, "Password must be provided."),
                    Arguments.of("", "Password must be provided."),
                    Arguments.of("a".repeat(17), "Password must be no more than 16 characters in length."),
                    Arguments.of("a".repeat(7), "Password must be 8 or more characters in length."),
                    Arguments.of("p4ssword@", "Password must contain 1 or more uppercase characters."),
                    Arguments.of("Password@", "Password must contain 1 or more digit characters."),
                    Arguments.of("P4ssword", "Password must contain 1 or more special characters."),
                    Arguments.of("P4ssssword@", "Password contains 4 occurrences of the character 's', but at most 3 are allowed."),
                    Arguments.of("P4ssword@abc", "Password contains the illegal alphabetical sequence 'abc'."),
                    Arguments.of("P4ssword@123", "Password contains the illegal numerical sequence '123'."),
                    Arguments.of("P4ssword@asd", "Password contains the illegal QWERTY sequence 'asd'."),
                    Arguments.of("P4ssword @", "Password contains a whitespace character.")
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidPasswordTestParameters.class)
    public void invalidPassword(String password, String violation) {
        ConstraintValidatorContext.ConstraintViolationBuilder specificConstraintViolationBuilder = mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.class
        );
        when(context.buildConstraintViolationWithTemplate(violation))
                .thenReturn(specificConstraintViolationBuilder);

        assertThat(validator.isValid(password, context)).isFalse();

        verify(context, times(1)).disableDefaultConstraintViolation();
        verify(context, times(1))
                .buildConstraintViolationWithTemplate(violation);
        verify(specificConstraintViolationBuilder, times(1))
                .addConstraintViolation();
    }
}
