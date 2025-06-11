package com.my.goldmanager.cors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsPreflightSpringBootTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightRequestAllowed() throws Exception {
        mockMvc.perform(options("/api/items")
                .header("Access-Control-Request-Method", "POST")
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }
}
