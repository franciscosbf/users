package com.e_commerce.users.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Provided user wasn't found.");
    }
}
