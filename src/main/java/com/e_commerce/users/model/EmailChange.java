package com.e_commerce.users.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailChange(
        @NotNull(message = "Credentials must be provided.")
        @Valid
        UserCredentials credentials,

        @NotEmpty(message = "Email must be provided.")
        @Email(message = "Email is invalid.")
        @Size(max = 254, message = "Email must contain between 3 and 254 characters.")
        String email) {

}
