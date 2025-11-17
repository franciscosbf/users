package com.e_commerce.users;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserCredentials(
        @Size(max = 64, message = "Username can only contain up to 64 characters.")
        @NotEmpty(message = "Username must be provided.")
        String username,

        @Size(max = 16, message = "Password can only contain up to 16 characters.")
        @NotEmpty(message = "Password must be provided.")
        String password) {

}
