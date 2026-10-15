package com.ledgersense.exception;

public class TooManyAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(Long retryAfterSeconds) {
        super("Too many login attempts. Try again later.");
        this.retryAfterSeconds = retryAfterSeconds;

    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
