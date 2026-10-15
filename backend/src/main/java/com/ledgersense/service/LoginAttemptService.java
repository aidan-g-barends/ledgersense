package com.ledgersense.service;

import com.ledgersense.exception.TooManyAttemptsException;
import com.ledgersense.service.impl.ILoginAttemptService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService implements ILoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    // NEW: the bouncer's notepad. One row per key: how many fails, and when counting started.
    private record Attempts(int failures, Instant windowStart) {}

    // WHY ConcurrentHashMap: many requests hit this at the same time (multiple threads).
    // A normal HashMap can corrupt itself under that.
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    @Override
    public void checkAllowed(String key) {
        Attempts current = attempts.get(key);
        if (current == null) {
            return;
        }
        if (isExpired(current)) {
            attempts.remove(key);   // 15 minutes passed, clean slate
            return;
        }
        if (current.failures() >= MAX_FAILURES) {
            long secondsLeft = Duration.between(Instant.now(), current.windowStart().plus(WINDOW)).toSeconds();
            throw new TooManyAttemptsException(Math.max(secondsLeft, 1));
        }
    }

    @Override
    public void recordFailure(String key) {
        // WHY: stops the map growing forever if someone sprays thousands of fake emails
        if (attempts.size() > 10_000) {
            attempts.values().removeIf(this::isExpired);
        }
        // WHY compute: read + update happens as ONE step, so two fails at the same instant
        // can't both read "3" and both write "4".
        attempts.compute(key, (k, current) ->
                current == null || isExpired(current)
                        ? new Attempts(1, Instant.now())
                        : new Attempts(current.failures() + 1, current.windowStart()));
    }

    @Override
    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private boolean isExpired(Attempts a) {
        return Instant.now().isAfter(a.windowStart().plus(WINDOW));
    }
}