package com.ledgersense.controller;

import com.ledgersense.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
// NEW: a throwaway Postgres in Docker, so tests don't touch your dev database
@Import(TestcontainersConfiguration.class)
// NEW: undoes each test's data afterwards
@Transactional
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create() throws Exception {
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "West Coast Solutions",
                                    "baseCurrency": "zar"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("West Coast Solutions"))
                .andExpect(jsonPath("$.baseCurrency").value("ZAR"));
    }

    @Test
    void create_whenNameBlank() throws Exception {
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "baseCurrency": "ZAR"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void read_whenMissing() throws Exception {
        mockMvc.perform(get("/api/organizations/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}