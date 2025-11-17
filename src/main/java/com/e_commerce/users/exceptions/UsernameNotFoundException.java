package com.e_commerce.users.exceptions;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username) {
        super("Username " + username + " wasn't found.");
    }
}
