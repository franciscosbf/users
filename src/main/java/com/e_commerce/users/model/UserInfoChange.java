package com.e_commerce.users.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserInfoChange(
        @NotEmpty(message = "Username must be provided.")
        @Size(max = 64, message = "Username can only contain up to 64 characters.")
        String username,

        @NotNull(message = "NewUserInfo must be provided.")
        @Valid
        UserInfo userInfo) {

}
