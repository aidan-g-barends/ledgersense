package com.ledgersense.controller;

import com.jayway.jsonpath.JsonPath;
import com.ledgersense.TestcontainersConfiguration;
import com.ledgersense.support.TestAuth;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class InsightsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private void importFixture(Cookie token) throws Exception {
        String accountJson = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cheque","bank":"ABSA","accountLast4":"3001"}
                                """))
                .andReturn().getResponse().getContentAsString();
        String accountId = JsonPath.read(accountJson, "$.id");

        byte[] fixture = getClass().getResourceAsStream("/fixtures/absa-statement-sample.csv").readAllBytes();
        mockMvc.perform(multipart("/api/imports")
                        .file(new MockMultipartFile("file", "absa.csv", "text/csv", fixture))
                        .with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated());
    }

    // Org A sees its October numbers. Org B (no data) sees zero, never A's.
    @Test
    void summary_latestMonthAndOrgScoped() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Insight A", "insights-a@ledger.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Insight B", "insights-b@ledger.co.za");
        importFixture(orgA);

        mockMvc.perform(get("/api/insights/summary").cookie(orgA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-10"))
                .andExpect(jsonPath("$.transactionCount").value(6))
                .andExpect(jsonPath("$.moneyIn").value(4200.0))
                .andExpect(jsonPath("$.months.length()").value(6));

        mockMvc.perform(get("/api/insights/summary").cookie(orgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(0));
    }

    // NEW
    @Test
    void timeline_thirtyDays() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Timeline Co", "timeline@ledger.co.za");
        importFixture(token);

        mockMvc.perform(get("/api/insights/timeline").param("days", "30").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(30))
                .andExpect(jsonPath("$[0].date").value("2026-09-15"))
                .andExpect(jsonPath("$[29].date").value("2026-10-14"))
                .andExpect(jsonPath("$[29].runningNet").value(3458.41));

        mockMvc.perform(get("/api/insights/timeline").param("days", "5").cookie(token))
                .andExpect(status().isBadRequest());
    }
}