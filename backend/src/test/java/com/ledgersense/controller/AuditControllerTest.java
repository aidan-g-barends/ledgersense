package com.ledgersense.controller;

import com.ledgersense.TestcontainersConfiguration;
import com.ledgersense.support.TestAuth;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void latest_recordsRegisterAndLogin() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Audit Co", "events@audit.co.za");

        mockMvc.perform(get("/api/audit-events").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItem("REGISTER")))
                .andExpect(jsonPath("$[*].action", hasItem("LOGIN_SUCCESS")));
    }

    // NEW: isolation. Org A's actions never show in org B's audit log.
    @Test
    void latest_onlyOwnOrg() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "audit-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "audit-b@shop.co.za");

        mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(orgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Cheque","bank":"FNB","accountLast4":"7777"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/audit-events").cookie(orgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", not(hasItem("BANK_ACCOUNT_CREATED"))));
    }
}