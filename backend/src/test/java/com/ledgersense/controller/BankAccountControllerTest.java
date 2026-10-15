package com.ledgersense.controller;

import com.jayway.jsonpath.JsonPath;
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
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Same helper as OrganizationControllerTest. We'll pull it into a shared class
    // once a third test needs it (rule of three).
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

    // NEW: creates an account and returns its id (pulled out of the JSON response)
    private String createAccount(Cookie token, String last4) throws Exception {
        String json = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Business Cheque","bank":"FNB","accountLast4":"%s"}
                                """.formatted(last4)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    @Test
    void create() throws Exception {
        Cookie token = loginAs("Bank Co", "create@bankco.co.za");

        mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Business Cheque","bank":"FNB","accountLast4":"4821"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.accountLast4").value("4821"))
                .andExpect(jsonPath("$.currency").value("ZAR"));
    }

    @Test
    void create_withFullAccountNumber() throws Exception {
        Cookie token = loginAs("Bank Co", "fullnumber@bankco.co.za");

        mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Business Cheque","bank":"FNB","accountLast4":"62012345678"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // NEW: isolation test 1. Org B's list never shows org A's accounts.
    @Test
    void readAll_onlyOwnOrg() throws Exception {
        Cookie orgA = loginAs("Shop A", "list-a@shop.co.za");
        Cookie orgB = loginAs("Shop B", "list-b@shop.co.za");
        createAccount(orgA, "1111");

        mockMvc.perform(get("/api/bank-accounts").cookie(orgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // NEW: isolation test 2. Org B guesses org A's exact id and still gets 404.
    @Test
    void read_otherOrgsAccount() throws Exception {
        Cookie orgA = loginAs("Shop A", "read-a@shop.co.za");
        Cookie orgB = loginAs("Shop B", "read-b@shop.co.za");
        String accountId = createAccount(orgA, "2222");

        mockMvc.perform(get("/api/bank-accounts/" + accountId).cookie(orgB))
                .andExpect(status().isNotFound());
    }
}