package com.e_commerce.users.exceptions;

public class DuplicatedUsernameException extends RuntimeException {
    public DuplicatedUsernameException(String username) {
        super("Username '" + username + "' is already being used.");
    }
}
