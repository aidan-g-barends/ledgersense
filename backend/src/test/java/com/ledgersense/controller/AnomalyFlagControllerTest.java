package com.ledgersense.controller;

import com.jayway.jsonpath.JsonPath;
import com.ledgersense.TestcontainersConfiguration;
import com.ledgersense.domain.BankTransaction;
import com.ledgersense.domain.ImportBatch;
import com.ledgersense.factory.BankTransactionFactory;
import com.ledgersense.factory.ImportBatchFactory;
import com.ledgersense.repository.BankTransactionRepository;
import com.ledgersense.repository.ImportBatchRepository;
import com.ledgersense.service.impl.IAnomalyDetectionService;
import com.ledgersense.support.TestAuth;
import com.ledgersense.util.TransactionFingerprint;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
class AnomalyFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportBatchRepository importBatchRepository;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private IAnomalyDetectionService anomalyDetectionService;

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

    // Two identical R299 charges on one day, then detection runs, exactly like an import
    private String seedDuplicateAndGetFlagId(Cookie token, String last4) throws Exception {
        UUID orgId = orgIdOf(token);
        UUID accountId = createAccount(token, last4);
        ImportBatch batch = importBatchRepository.save(
                ImportBatchFactory.createImportBatch(orgId, accountId, "dup.csv"));
        LocalDate day = LocalDate.now().minusDays(2);

        List<BankTransaction> saved = bankTransactionRepository.saveAll(List.of(
                charge(orgId, accountId, batch.getId(), day, 1),
                charge(orgId, accountId, batch.getId(), day, 2)));
        anomalyDetectionService.detect(orgId, saved);

        String json = mockMvc.perform(get("/api/flags").cookie(token))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$[0].id");
    }

    private BankTransaction charge(UUID orgId, UUID accountId, UUID batchId, LocalDate day, int occurrence) {
        BigDecimal amount = new BigDecimal("-299.00");
        String fp = TransactionFingerprint.of(accountId, day, amount, "CLOUD HOST LTD", occurrence);
        return BankTransactionFactory.createBankTransaction(orgId, accountId, batchId, day,
                "CLOUD HOST LTD", amount, null, fp);
    }

    @Test
    void list_showsDuplicate() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Flag Co", "flags-list@flag.co.za");
        seedDuplicateAndGetFlagId(token, "2001");

        mockMvc.perform(get("/api/flags").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rule").value("DUPLICATE_CHARGE"))
                .andExpect(jsonPath("$[0].transaction.amount").value(-299.00));
    }

    // Review it, and it moves tabs. Another org gets 404, not 403.
    @Test
    void review_movesFlagAndIsOrgScoped() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Flag A", "flags-a@flag.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Flag B", "flags-b@flag.co.za");
        String flagId = seedDuplicateAndGetFlagId(orgA, "2002");

        mockMvc.perform(patch("/api/flags/" + flagId + "/review").with(csrf()).cookie(orgB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DISPUTED"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/flags/" + flagId + "/review").with(csrf()).cookie(orgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"LEGITIMATE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LEGITIMATE"));

        mockMvc.perform(get("/api/flags").param("status", "LEGITIMATE").cookie(orgA))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void history_returnsBothCharges() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Flag Co", "flags-history@flag.co.za");
        String flagId = seedDuplicateAndGetFlagId(token, "2003");

        mockMvc.perform(get("/api/flags/" + flagId + "/history").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}