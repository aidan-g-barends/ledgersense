package com.ledgersense.service.impl;

public interface ILoginAttemptService {

    void checkAllowed(String key);

    void recordFailure(String key);

    void recordSuccess(String key);
}
