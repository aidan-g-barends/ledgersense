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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class CategoryRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // NEW: grab the id of one of this org's seeded categories
    private String firstCategoryId(Cookie token) throws Exception {
        String json = mockMvc.perform(get("/api/categories").cookie(token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$[0].id");
    }

    private String createRuleJson(String categoryId, String keyword) {
        return """
                {"categoryId":"%s","keyword":"%s"}
                """.formatted(categoryId, keyword);
    }

    @Test
    void create() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Rule Co", "create@ruleco.co.za");
        String categoryId = firstCategoryId(token);

        mockMvc.perform(post("/api/category-rules").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRuleJson(categoryId, "woolworths")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keyword").value("WOOLWORTHS"));
    }

    // NEW: org B tries to attach a rule to org A's category, so 404
    @Test
    void create_withOtherOrgsCategory() throws Exception {
        Cookie orgA = TestAuth.loginAs(mockMvc, "Shop A", "rule-a@shop.co.za");
        Cookie orgB = TestAuth.loginAs(mockMvc, "Shop B", "rule-b@shop.co.za");
        String orgACategoryId = firstCategoryId(orgA);

        mockMvc.perform(post("/api/category-rules").with(csrf()).cookie(orgB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRuleJson(orgACategoryId, "SHELL")))
                .andExpect(status().isNotFound());
    }

    // CHANGED: named deleteRule, not delete. A method called delete() hides the
    // static MockMvc delete(...) import, so line 78 called this test instead.
    @Test
    void deleteRule() throws Exception {
        Cookie token = TestAuth.loginAs(mockMvc, "Rule Co", "delete@ruleco.co.za");
        String categoryId = firstCategoryId(token);

        String json = mockMvc.perform(post("/api/category-rules").with(csrf()).cookie(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRuleJson(categoryId, "ENGEN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ruleId = JsonPath.read(json, "$.id");

        mockMvc.perform(delete("/api/category-rules/" + ruleId).with(csrf()).cookie(token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/category-rules").cookie(token))
                .andExpect(jsonPath("$.length()").value(0));
    }
}