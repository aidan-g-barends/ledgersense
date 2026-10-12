package com.ledgersense.service.impl;

import com.ledgersense.domain.AppUser;

public interface IAuthService {

    AppUser register(String businessName, String email, String password);
}
