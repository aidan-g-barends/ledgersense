package com.ledgersense.support;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// NEW: one shared "register + login" helper for every controller test.
// WHY: copy-pasted test code drifts apart. One place = one fix.
public final class TestAuth {

    public static final String PASSWORD = "correct-horse-battery";

    private TestAuth() {
    }

    public static Cookie loginAs(MockMvc mockMvc, String businessName, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"%s","email":"%s","password":"%s"}
                                """.formatted(businessName, email, PASSWORD)))
                .andExpect(status().isCreated());

        return mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }
}
