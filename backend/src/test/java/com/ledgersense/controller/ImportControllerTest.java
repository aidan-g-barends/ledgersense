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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final MockMultipartFile simpleCsv = new MockMultipartFile("file", "statement.csv", "text/csv", """
            Date,Description,Amount
            2026/10/01,WOOLWORTHS CPT,-450.50
            2026/13/01,BAD MONTH,-10.00
            2026/10/03,ENGEN GARAGE,-800.00
            """.getBytes(StandardCharsets.UTF_8));

    // NEW: the committed fake Absa statement from src/test/resources/fixtures
    private MockMultipartFile absaFixture() throws IOException {
        byte[] bytes = getClass().getResourceAsStream("/fixtures/absa-statement-sample.csv").readAllBytes();
        return new MockMultipartFile("file", "absa-statement-sample.csv", "text/csv", bytes);
    }

    private String createAccount(Cookie token, String bank, String last4) throws Exception {
        String json = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cheque","bank":"%s","accountLast4":"%s"}
                                """.formatted(bank, last4)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    @Test
    void preview() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Import Co", "preview@import.co.za");
        String accountId = createAccount(token, "FNB", "3001");

        mockMvc.perform(multipart("/api/imports/preview").file(simpleCsv).with(csrf()).cookie(token)
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

    @Test
    void preview_withOtherOrgsAccount() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "import-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "import-b@shop.co.za");
        String orgAAccount = createAccount(orgA, "FNB", "3002");

        mockMvc.perform(multipart("/api/imports/preview").file(simpleCsv).with(csrf()).cookie(orgB)
                        .param("bankAccountId", orgAAccount)
                        .param("delimiter", ",")
                        .param("datePattern", "yyyy/MM/dd")
                        .param("dateColumn", "0")
                        .param("descriptionColumn", "1")
                        .param("amountColumn", "2"))
                .andExpect(status().isNotFound());
    }

    // NEW: THE duplicate test. Same file twice = 12 new rows, then 0 new rows.
    // No mapping params sent, so the Absa preset is used automatically.
    @Test
    void commit_secondImportSkipsDuplicates() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Import Co", "twice@import.co.za");
        String accountId = createAccount(token, "ABSA", "3003");

        mockMvc.perform(multipart("/api/imports").file(absaFixture()).with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(12)) // both identical coffees kept
                .andExpect(jsonPath("$.duplicates").value(0));

        mockMvc.perform(multipart("/api/imports").file(absaFixture()).with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(0))
                .andExpect(jsonPath("$.duplicates").value(12));

        mockMvc.perform(get("/api/transactions").cookie(token))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.content[0].merchantId").isNotEmpty()); // NEW: merchants linked
    }

    // NEW: a rule created BEFORE import categorises matching rows DURING import
    @Test
    void commit_autoCategorises() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Import Co", "rules@import.co.za");
        String accountId = createAccount(token, "ABSA", "3004");

        String categories = mockMvc.perform(get("/api/categories").cookie(token))
                .andReturn().getResponse().getContentAsString();
        List<String> groceriesIds = JsonPath.read(categories, "$[?(@.name == 'Groceries')].id");
        String groceriesId = groceriesIds.get(0);

        mockMvc.perform(post("/api/category-rules").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","keyword":"fresh mart"}
                                """.formatted(groceriesId)))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/imports").file(absaFixture()).with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions").param("q", "fresh mart").cookie(token))
                .andExpect(jsonPath("$.content[0].categoryId").value(groceriesId));
    }

    // NEW: history shows the completed batch with its stats
    @Test
    void history() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Import Co", "history@import.co.za");
        String accountId = createAccount(token, "ABSA", "3005");

        mockMvc.perform(multipart("/api/imports").file(absaFixture()).with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/imports").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].rowsImported").value(12));
    }
}