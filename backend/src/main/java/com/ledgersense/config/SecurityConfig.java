package com.ledgersense.config;

import com.ledgersense.security.CookieBearerTokenResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // NEW: the rules for which URLs need a logged-in user.
    // (The "x -> x.disable()" parts are lambdas: Spring Security's config style requires them.)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CHANGED: login added to the open list
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/actuator/health", "/error").permitAll()
                        .requestMatchers("/api/organizations/**").permitAll()
                        .anyRequest().authenticated())
                // NEW: check the JWT (from our cookie) on every protected request.
                // Missing / expired / tampered token -> 401 automatically.
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(new CookieBearerTokenResolver())
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
