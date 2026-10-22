package com.ledgersense.controller;

import com.ledgersense.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
// CHANGED: no class-level @Transactional. AuditService.record() runs in its own transaction
// (REQUIRES_NEW), and it can't see rows from a test transaction that never commits, so the
// audit insert failed its foreign keys. Tests now commit for real, like the other controller tests.
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // CHANGED: a fresh email per test (JUnit makes a new instance for every test),
    // because rows are no longer rolled back between tests
    private final String id = UUID.randomUUID().toString();

    private final String validBody = """
            {
                "businessName": "Digital Solutions WC",
                "email": "Aidan+%s@DigitalSolutions.co.za",
                "password": "correct-horse-battery"
            }
            """.formatted(id);

    private final String loginBody = """
            {
                "email": "aidan+%s@digitalsolutions.co.za",
                "password": "correct-horse-battery"
            }
            """.formatted(id);

    private MvcResult registerAndLogin() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(validBody));

        return mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andReturn();
    }

    @Test
    void register() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(validBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("aidan+" + id + "@digitalsolutions.co.za"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // NEW: proves CSRF protection is ON
    @Test
    void register_withoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(validBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void login() throws Exception {
        MvcResult result = registerAndLogin();

        Cookie access = result.getResponse().getCookie("access_token");
        Cookie refresh = result.getResponse().getCookie("refresh_token");
        assertNotNull(access);
        assertNotNull(refresh);
        assertTrue(access.isHttpOnly());
        assertTrue(refresh.isHttpOnly());
    }

    @Test
    void me() throws Exception {
        Cookie access = registerAndLogin().getResponse().getCookie("access_token");

        mockMvc.perform(get("/api/auth/me").cookie(access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Digital Solutions WC"));
    }

    @Test
    void me_withoutLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh() throws Exception {
        Cookie refresh = registerAndLogin().getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refresh))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"));
    }

    // NEW: using the SAME refresh token twice = reuse = rejected
    @Test
    void refresh_whenTokenReused() throws Exception {
        Cookie refresh = registerAndLogin().getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refresh))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refresh))
                .andExpect(status().isUnauthorized());
    }

    // NEW: after logout, the refresh token no longer works
    @Test
    void logout() throws Exception {
        Cookie refresh = registerAndLogin().getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/logout").with(csrf()).cookie(refresh))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").with(csrf()).cookie(refresh))
                .andExpect(status().isUnauthorized());
    }

    // NEW: 5 wrong passwords, then even the RIGHT password gets 429
    @Test
    void login_tooManyAttempts() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"Locked Ltd","email":"locked@acme.co.za","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isCreated());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"locked@acme.co.za","password":"wrong-password-123"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"locked@acme.co.za","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
