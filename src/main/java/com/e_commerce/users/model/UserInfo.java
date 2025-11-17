package com.e_commerce.users.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserInfo(
        @NotEmpty(message = "Description must be provided.")
        @Size(max = 200, message = "Description must contain up to 200 characters.")
        String description) {

}
