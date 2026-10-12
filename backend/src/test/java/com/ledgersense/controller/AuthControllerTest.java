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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_BODY = """
            {
                "businessName": "Digital Solutions WC",
                "email": "Aidan@DigitalSolutions.co.za",
                "password": "correct-horse-battery"
            }
            """;

    @Test
    void register() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("aidan@digitalsolutions.co.za"))
                // Security check: the hash must NEVER appear in a response
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_whenEmailTaken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void register_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "businessName": "Digital Solutions WC",
                                    "email": "aidan@digitalsolutions.co.za",
                                    "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Helper: registers, logs in, returns the cookie
    private Cookie registerAndLogin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "aidan@digitalsolutions.co.za",
                                "password": "correct-horse-battery"
                            }
                            """))
                .andReturn();

        return result.getResponse().getCookie("access_token");
    }

    @Test
    void login() throws Exception {
        Cookie cookie = registerAndLogin();

        assertNotNull(cookie);
        // Security checks on the cookie itself
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }

    @Test
    void login_whenWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "email": "aidan@digitalsolutions.co.za",
                                "password": "wrong-password-123"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me() throws Exception {
        Cookie cookie = registerAndLogin();

        mockMvc.perform(get("/api/auth/me").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("aidan@digitalsolutions.co.za"))
                .andExpect(jsonPath("$.organizationName").value("Digital Solutions WC"));
    }

    // Proves the endpoint is actually protected
    @Test
    void me_withoutLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}