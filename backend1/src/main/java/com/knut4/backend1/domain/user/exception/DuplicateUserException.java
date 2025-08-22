package com.knut4.backend1.domain.user.exception;

public class DuplicateUserException extends RuntimeException {
    
    public DuplicateUserException(String username) {
        super("User with username '" + username + "' already exists");
    }
    
    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}