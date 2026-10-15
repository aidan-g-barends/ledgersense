package com.ledgersense.service;

import com.ledgersense.exception.TooManyAttemptsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    private static final String KEY = "owner@acme.co.za|127.0.0.1";
    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void checkAllowed_underLimit() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(KEY);
        }
        service.checkAllowed(KEY); // no exception = pass
    }

    @Test
    void checkAllowed_afterMaxFailures() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(KEY);
        }
        assertThrows(TooManyAttemptsException.class, () -> service.checkAllowed(KEY));
    }

    @Test
    void recordSuccess_resetsCounter() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(KEY);
        }
        service.recordSuccess(KEY);
        service.checkAllowed(KEY);
    }
}