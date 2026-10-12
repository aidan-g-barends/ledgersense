package com.ledgersense.security;

import com.ledgersense.domain.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private JwtEncoder jwtEncoder;
    private Duration accessTokenTtl;

    public JwtService(JwtEncoder jwtEncoder,@Value("${app.jwt.access-token-minutes:15}") long accessTokenMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = Duration.ofMinutes(accessTokenMinutes);
    }

    public String issueAccessToken(AppUser user){
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ledgersense")
                .subject(user.getId().toString())          // WHO: the user id
                .claim("org", user.getOrgId().toString())  // WHICH org (used for scoping on Thursday)
                .claim("role", user.getRole().name())
                .issuedAt(now)
                // SHORT on purpose: a stolen token is useless after 15 minutes
                .expiresAt(now.plus(accessTokenTtl))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }
}
