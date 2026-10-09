package com.ledgersense.factory;

import com.ledgersense.domain.AppUser;
import com.ledgersense.domain.Role;
import com.ledgersense.util.Helper;

import java.util.Locale;
import java.util.UUID;

public class AppUserFactory {

    public static AppUser createAppUser(UUID orgId, String email, String passwordHash, Role role){
        if(orgId == null){
            return null;
        }

        if(!Helper.isValidEmail(email) || !Helper.isWithinLength(email.strip(), 254)){
            return null;
        }

        if(role == null){
            return null;
        }

        return new AppUser.Builder()
                .setOrgId(orgId)
                .setEmail(email.strip().toLowerCase(Locale.ROOT))
                .setPasswordHash(passwordHash)
                .setRole(role)
                .build();
    }
}
