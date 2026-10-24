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

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Fake data only. One bad date row on purpose.
    private final MockMultipartFile file = new MockMultipartFile("file", "statement.csv", "text/csv", """
            Date,Description,Amount
            2026/10/01,WOOLWORTHS CPT,-450.50
            2026/13/01,BAD MONTH,-10.00
            2026/10/03,ENGEN GARAGE,-800.00
            """.getBytes(StandardCharsets.UTF_8));

    private String createAccount(Cookie token, String last4) throws Exception {
        String json = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Cheque","bank":"FNB","accountLast4":"%s"}
                                """.formatted(last4)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    @Test
    void preview() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Import Co", "preview@import.co.za");
        String accountId = createAccount(token, "3001");

        // NEW: multipart() builds a file-upload request. .param() adds the form fields.
        mockMvc.perform(multipart("/api/imports/preview").file(file).with(csrf()).cookie(token)
                        .param("bankAccountId", accountId)
                        .param("delimiter", ",")
                        .param("datePattern", "yyyy/MM/dd")
                        .param("dateColumn", "0")
                        .param("descriptionColumn", "1")
                        .param("amountColumn", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(3))
                .andExpect(jsonPath("$.validRows").value(2))
                .andExpect(jsonPath("$.errorCount").value(1))
                .andExpect(jsonPath("$.errors[0].rowNumber").value(3));
    }

    // NEW: isolation. Org B can't preview into org A's bank account.
    @Test
    void preview_withOtherOrgsAccount() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "import-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "import-b@shop.co.za");
        String orgAAccount = createAccount(orgA, "3002");

        mockMvc.perform(multipart("/api/imports/preview").file(file).with(csrf()).cookie(orgB)
                        .param("bankAccountId", orgAAccount)
                        .param("delimiter", ",")
                        .param("datePattern", "yyyy/MM/dd")
                        .param("dateColumn", "0")
                        .param("descriptionColumn", "1")
                        .param("amountColumn", "2"))
                .andExpect(status().isNotFound());
    }
}