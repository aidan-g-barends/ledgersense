package com.ledgersense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain secuirtyFilterChain(HttpSecurity http) throws Exception{

        http
                // CSRF OFF for now. CSRF attacks need cookies that the browser sends
                // automatically. We have no cookies yet. Wednesday (login cookies) turns it ON.

                .csrf(csrf -> csrf.disable())

                // No Spring login page and no browser username/password popup: we build our own API login
                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                // STATELESS = the server keeps no sessions. Each request proves who it is (JWT, Wednesday).
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->auth

                        //Open to Everyone
                        .requestMatchers("/api/auth/register", "/actuator/health", "/error").permitAll()

                        .requestMatchers("/api/organizations/**").permitAll()

                        // EVERYTHING else needs a logged-in user (secure by default)
                        .anyRequest().authenticated());

                return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
