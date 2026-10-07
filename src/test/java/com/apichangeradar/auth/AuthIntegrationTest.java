package com.apichangeradar.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthIntegrationTest {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("app.scheduler.enabled", () -> false);
    }

    @Autowired MockMvc mvc;

    @Test
    void registerAndLogin() throws Exception {
        String reg = "{\"name\":\"Test\",\"email\":\"test@example.com\",\"password\":\"password123\"}";
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg)).andExpect(status().isCreated());
        String login = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login)).andExpect(status().isOk());
    }
}

