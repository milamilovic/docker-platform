package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.LogSearchResponse;
import com.dockerplatform.backend.models.LogEntry;
import com.dockerplatform.backend.service.AnalyticsService;
import com.dockerplatform.backend.service.CacheService;
import com.dockerplatform.backend.service.RegistryTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private RegistryTokenService tokenService;

    @MockitoBean
    private CacheService cacheService;

    @MockitoBean
    private AnalyticsService analyticsService;

    @BeforeEach
    void setup() throws IOException {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Path adminSecret = Paths.get("secrets", "super_admin.txt");

        if (Files.exists(adminSecret)) {
            Files.delete(adminSecret);
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testSearchLogs_Success() throws Exception {

        LogEntry entry = new LogEntry();
        entry.setTimestamp("2025-01-01T12:00:00Z");
        entry.setLevel("ERROR");
        entry.setMessage("Database connection failed");
        entry.setLogger("com.backend.DatabaseService");
        entry.setThread("main");
        entry.setStackTrace("java.sql.SQLException...");

        LogSearchResponse response = new LogSearchResponse();
        response.setHits(List.of(entry));
        response.setTotal(1L);

        when(analyticsService.searchLogs(any())).thenReturn(response);

        mockMvc.perform(get("/analytics/logs")
                        .param("query", "database")
                        .param("size", "10")
                        .param("from", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.hits", hasSize(1)))
                .andExpect(jsonPath("$.hits[0].message",
                        is("Database connection failed")))
                .andExpect(jsonPath("$.hits[0].level", is("ERROR")))
                .andExpect(jsonPath("$.hits[0].logger_name",
                        is("com.backend.DatabaseService")))
                .andExpect(jsonPath("$.hits[0].thread_name", is("main")))
                .andExpect(jsonPath("$.hits[0].stack_trace",
                        containsString("SQLException")));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"REGULAR"})
    void testSearchLogs_AsRegularUser_Forbidden() throws Exception {

        mockMvc.perform(get("/analytics/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testExportLogs_Success() throws Exception {
        byte[] fakeExport =
                "[{\"message\":\"Exported log\"}]".getBytes();

        when(analyticsService.exportLogs(any()))
                .thenReturn(fakeExport);

        String requestBody = """
                {
                    "query": "error",
                    "size": 50,
                    "from": 0
                }
                """;

        mockMvc.perform(post("/analytics/logs/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment")))
                .andExpect(header().string("Content-Type",
                        containsString("application/json")))
                .andExpect(content().bytes(fakeExport));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testSearchLogs_NoQueryParam_ReturnsAll() throws Exception {

        LogEntry entry = new LogEntry();
        entry.setTimestamp("2025-01-01T12:00:00Z");
        entry.setLevel("INFO");
        entry.setMessage("System started");

        LogSearchResponse response = new LogSearchResponse();
        response.setHits(List.of(entry));
        response.setTotal(1L);

        when(analyticsService.searchLogs(any())).thenReturn(response);

        mockMvc.perform(get("/analytics/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.hits[0].message",
                        is("System started")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testSearchLogs_FilterByLevel() throws Exception {
        LogEntry entry = new LogEntry();
        entry.setLevel("ERROR");
        entry.setMessage("Critical failure");

        LogSearchResponse response = new LogSearchResponse();
        response.setHits(List.of(entry));
        response.setTotal(1L);

        when(analyticsService.searchLogs(any())).thenReturn(response);
        mockMvc.perform(get("/analytics/logs")
                        .param("levels", "ERROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits[0].level", is("ERROR")))
                .andExpect(jsonPath("$.hits[0].message",
                        is("Critical failure")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testSearchLogs_FilterByDateRange() throws Exception {
        LogEntry entry = new LogEntry();
        entry.setTimestamp("2025-01-05T10:00:00Z");
        entry.setMessage("Log inside range");

        LogSearchResponse response = new LogSearchResponse();
        response.setHits(List.of(entry));
        response.setTotal(1L);

        when(analyticsService.searchLogs(any())).thenReturn(response);

        mockMvc.perform(get("/analytics/logs")
                        .param("startDate", "2025-01-01T00:00:00Z")
                        .param("endDate", "2025-01-10T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits[0].message",
                        is("Log inside range")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testSearchLogs_NoResults_ReturnsEmptyHits() throws Exception {
        LogSearchResponse response = new LogSearchResponse();
        response.setHits(List.of());
        response.setTotal(0L);

        when(analyticsService.searchLogs(any())).thenReturn(response);

        mockMvc.perform(get("/analytics/logs")
                        .param("query", "somethingThatDoesNotExist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.hits", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"REGULAR"})
    void testExportLogs_AsRegularUser_Forbidden() throws Exception {

        mockMvc.perform(post("/analytics/logs/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"error\"}"))
                .andExpect(status().isForbidden());
    }

}
