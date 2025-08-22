package com.knut4.backend1.domain.user.exception;

public class AuthenticationFailedException extends RuntimeException {
    
    public AuthenticationFailedException(String message) {
        super(message);
    }
    
    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}