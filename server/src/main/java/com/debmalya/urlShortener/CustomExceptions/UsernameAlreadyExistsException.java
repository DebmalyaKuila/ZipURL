package com.debmalya.urlShortener.CustomExceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username already exists");
    }
}
