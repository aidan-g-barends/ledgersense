package com.ledgersense.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {

        super("Invalid email or password");
    }

    // NEW: second constructor so refresh can say something more accurate than "wrong password"
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
