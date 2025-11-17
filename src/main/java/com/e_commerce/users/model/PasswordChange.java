package com.e_commerce.users.model;

import com.e_commerce.users.constraints.PasswordConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PasswordChange(
        @NotNull(message = "Credentials must be provided.")
        @Valid
        UserCredentials credentials,

        @PasswordConstraint
        String password) {

}
