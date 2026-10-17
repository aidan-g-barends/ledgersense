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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // NEW: proves seeding ran during register
    @Test
    void readAll_hasDefaultsAfterRegister() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Cat Co", "defaults@catco.co.za");

        mockMvc.perform(get("/api/categories").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Groceries")))
                .andExpect(jsonPath("$[*].name", hasItem("Sales")));
    }

    @Test
    void create() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Cat Co", "create@catco.co.za");

        mockMvc.perform(post("/api/categories").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Equipment","type":"EXPENSE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Equipment"));
    }

    // NEW: "groceries" clashes with the seeded "Groceries" because the check ignores case
    @Test
    void create_duplicateIgnoringCase() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Cat Co", "dupe@catco.co.za");

        mockMvc.perform(post("/api/categories").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"groceries","type":"EXPENSE"}
                                """))
                .andExpect(status().isConflict());
    }

    // NEW: isolation. Org B can't read org A's category even with the exact id.
    @Test
    void read_otherOrgsCategory() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "cat-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "cat-b@shop.co.za");

        String json = mockMvc.perform(post("/api/categories").with(csrf()).cookie(orgA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Equipment","type":"EXPENSE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String categoryId = JsonPath.read(json, "$.id");

        mockMvc.perform(get("/api/categories/" + categoryId).cookie(orgB))
                .andExpect(status().isNotFound());
    }
}
