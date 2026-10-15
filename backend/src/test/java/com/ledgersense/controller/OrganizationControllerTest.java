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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // NEW: helper that registers + logs in, and hands back the access_token cookie.
    // WHY: every endpoint here needs a logged-in user now.
    private Cookie loginAs(String businessName, String email) throws Exception {
        String password = "correct-horse-battery";

        mockMvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"%s","email":"%s","password":"%s"}
                                """.formatted(businessName, email, password)))
                .andExpect(status().isCreated());

        return mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("access_token");
    }

    @Test
    void read() throws Exception {
        Cookie token = loginAs("Acme Traders", "read@acme.co.za");

        mockMvc.perform(get("/api/organization").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Traders"))
                .andExpect(jsonPath("$.baseCurrency").value("ZAR"));
    }

    @Test
    void read_withoutLogin() throws Exception {
        mockMvc.perform(get("/api/organization"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rename() throws Exception {
        Cookie token = loginAs("Old Name", "rename@acme.co.za");

        mockMvc.perform(patch("/api/organization/name").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New Name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    // NEW: the isolation test. Org A changes something, org B must not be affected.
    @Test
    void rename_doesNotAffectOtherOrg() throws Exception {
        Cookie orgA = loginAs("Shop A", "owner@shop-a.co.za");
        Cookie orgB = loginAs("Shop B", "owner@shop-b.co.za");

        mockMvc.perform(patch("/api/organization/name").with(csrf()).cookie(orgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Shop A Renamed"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organization").cookie(orgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Shop B"));
    }
}