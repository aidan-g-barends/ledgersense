package com.ledgersense.controller;

import com.jayway.jsonpath.JsonPath;
import com.ledgersense.TestcontainersConfiguration;
import com.ledgersense.domain.ImportBatch;
import com.ledgersense.factory.BankTransactionFactory;
import com.ledgersense.factory.ImportBatchFactory;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.ImportBatchRepository;
import com.ledgersense.support.TestAuth;
import com.ledgersense.util.TransactionFingerprint;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BankTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportBatchRepository importBatchRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    // NEW: read the caller's orgId from /api/auth/me
    private UUID orgIdOf(Cookie token) throws Exception {
        String json = mockMvc.perform(get("/api/auth/me").cookie(token))
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(json, "$.organizationId"));
    }

    private UUID createAccount(Cookie token, String last4) throws Exception {
        String json = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"FNB Cheque","bank":"FNB","accountLast4":"%s"}
                                """.formatted(last4)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(json, "$.id"));
    }

    // NEW: puts one batch + two transactions in the DB, like a tiny import would
    private void seed(UUID orgId, UUID accountId) {
        ImportBatch batch = importBatchRepository.save(
                ImportBatchFactory.createImportBatch(orgId, accountId, "test.csv"));
        saveTx(orgId, accountId, batch.getId(), LocalDate.now().minusDays(5), "WOOLWORTHS CPT", "-450.50");
        saveTx(orgId, accountId, batch.getId(), LocalDate.now().minusDays(1), "ENGEN GARAGE", "-800.00");
    }

    private void saveTx(UUID orgId, UUID accountId, UUID batchId, LocalDate date, String desc, String amount) {
        BigDecimal money = new BigDecimal(amount);
        String fp = TransactionFingerprint.of(accountId, date, money, desc, 1);
        bankTransactionRepository.save(BankTransactionFactory.createBankTransaction(
                orgId, accountId, batchId, date, desc, money, null, fp));
    }

    @Test
    void list_newestFirst() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Ledger Co", "list@ledger.co.za");
        seed(orgIdOf(token), createAccount(token, "1001"));

        mockMvc.perform(get("/api/transactions").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].description").value("ENGEN GARAGE"));
    }

    @Test
    void list_searchByDescription() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Ledger Co", "search@ledger.co.za");
        seed(orgIdOf(token), createAccount(token, "1002"));

        mockMvc.perform(get("/api/transactions").param("q", "wool").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(-450.50));
    }

    // NEW: isolation. Org B sees zero of org A's transactions.
    @Test
    void list_onlyOwnOrg() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "tx-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "tx-b@shop.co.za");
        seed(orgIdOf(orgA), createAccount(orgA, "1003"));

        mockMvc.perform(get("/api/transactions").cookie(orgB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // NEW: change one coffee to Groceries + "remember", and BOTH identical coffees update
    @Test
    void recategorise_rememberMerchant() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Ledger Co", "remember@ledger.co.za");

        String accountJson = mockMvc.perform(post("/api/bank-accounts").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cheque","bank":"ABSA","accountLast4":"1004"}
                                """))
                .andReturn().getResponse().getContentAsString();
        String accountId = JsonPath.read(accountJson, "$.id");

        byte[] fixture = getClass().getResourceAsStream("/fixtures/absa-statement-sample.csv").readAllBytes();
        mockMvc.perform(multipart("/api/imports")
                        .file(new MockMultipartFile("file", "absa.csv", "text/csv", fixture))
                        .with(csrf()).cookie(token)
                        .param("bankAccountId", accountId))
                .andExpect(status().isCreated());

        String categories = mockMvc.perform(get("/api/categories").cookie(token))
                .andReturn().getResponse().getContentAsString();
        List<String> groceries = JsonPath.read(categories, "$[?(@.name == 'Groceries')].id");

        String coffees = mockMvc.perform(get("/api/transactions").param("q", "corner coffee").cookie(token))
                .andReturn().getResponse().getContentAsString();
        String firstCoffeeId = JsonPath.read(coffees, "$.content[0].id");

        mockMvc.perform(patch("/api/transactions/" + firstCoffeeId + "/category").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","rememberMerchant":true}
                                """.formatted(groceries.get(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantTransactionsUpdated").value(2));

        mockMvc.perform(get("/api/transactions").param("q", "corner coffee").cookie(token))
                .andExpect(jsonPath("$.content[1].categoryId").value(groceries.get(0)));
    }
}
