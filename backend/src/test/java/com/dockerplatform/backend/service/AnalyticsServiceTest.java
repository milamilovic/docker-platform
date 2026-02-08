package com.dockerplatform.backend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.dockerplatform.backend.dto.LogSearchQuery;
import com.dockerplatform.backend.dto.LogSearchResponse;
import com.dockerplatform.backend.models.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LogEntry testLog;

    @BeforeEach
    void setup() {
        testLog = new LogEntry();
        testLog.setTimestamp("2025-01-01T12:00:00Z");
        testLog.setLevel("ERROR");
        testLog.setMessage("Database failure");
        testLog.setLogger("com.backend.DatabaseService");
        testLog.setThread("main");
        testLog.setStackTrace("SQLException...");
    }

    // helper method for creating SearchResponse
    private SearchResponse<LogEntry> mockSearchResponse(List<Hit<LogEntry>> hits) {
        HitsMetadata<LogEntry> hitsMetadata = HitsMetadata.of(h -> h
                .hits(hits)
                .total(TotalHits.of(t -> t.value(hits.size()).relation(TotalHitsRelation.Eq)))
        );

        ShardStatistics shards = ShardStatistics.of(s ->
                s.total(1)
                .successful(1)
                .skipped(0)
                .failed(0));

        return SearchResponse.of(r -> r
                .hits(hitsMetadata)
                .took(5)
                .timedOut(false)
                .shards(shards)
        );
    }

    @Test
    void testSearchLogs_TextQuery_Success() throws Exception {
        Hit<LogEntry> hit = Hit.of(h -> h.index("docker-platform-logs-2025").id("1").source(testLog));
        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenReturn(mockSearchResponse(List.of(hit)));

        LogSearchQuery query = new LogSearchQuery();
        query.setQuery("database");
        query.setSize(10);
        query.setFrom(0);

        LogSearchResponse response = analyticsService.searchLogs(query);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals("Database failure", response.getHits().get(0).getMessage());
    }

    @Test
    void testSearchLogs_LevelFilter_Success() throws Exception {
        LogEntry infoLog = new LogEntry();
        infoLog.setLevel("INFO");
        infoLog.setMessage("Info message");

        Hit<LogEntry> hitError = Hit.of(h -> h.index("docker-platform-logs-2025").id("1").source(testLog));

        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenReturn(mockSearchResponse(List.of(hitError)));

        LogSearchQuery query = new LogSearchQuery();
        query.setLevels(List.of("ERROR"));
        query.setSize(10);
        query.setFrom(0);

        LogSearchResponse response = analyticsService.searchLogs(query);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals("ERROR", response.getHits().get(0).getLevel());
    }

    @Test
    void testSearchLogs_DateRangeFilter_Success() throws Exception {
        LogEntry oldLog = new LogEntry();
        oldLog.setTimestamp("2024-12-31T23:59:59Z");
        oldLog.setLevel("INFO");
        oldLog.setMessage("Old log");

        Hit<LogEntry> hitNew = Hit.of(h -> h.index("docker-platform-logs-2025").id("2").source(testLog));

        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenReturn(mockSearchResponse(List.of(hitNew)));

        LogSearchQuery query = new LogSearchQuery();
        query.setStartDate("2025-01-01T00:00:00Z");
        query.setEndDate("2025-01-02T00:00:00Z");
        query.setSize(10);
        query.setFrom(0);

        LogSearchResponse response = analyticsService.searchLogs(query);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals("Database failure", response.getHits().get(0).getMessage());
    }

    @Test
    void testSearchLogs_CombinedFilters_Success() throws Exception {
        LogEntry log1 = new LogEntry();
        log1.setLevel("ERROR");
        log1.setMessage("Database error");
        log1.setTimestamp("2025-01-01T10:00:00Z");

        Hit<LogEntry> hit = Hit.of(h -> h.index("docker-platform-logs-2025").id("1").source(log1));

        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenReturn(mockSearchResponse(List.of(hit)));

        LogSearchQuery query = new LogSearchQuery();
        query.setQuery("database");
        query.setLevels(List.of("ERROR"));
        query.setStartDate("2025-01-01T00:00:00Z");
        query.setEndDate("2025-01-02T00:00:00Z");
        query.setSize(10);
        query.setFrom(0);

        LogSearchResponse response = analyticsService.searchLogs(query);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals("Database error", response.getHits().get(0).getMessage());
        assertEquals("ERROR", response.getHits().get(0).getLevel());
    }

    @Test
    void testExportLogs_Success() throws Exception {
        Hit<LogEntry> hit = Hit.of(h -> h.index("docker-platform-logs-2025").id("1").source(testLog));

        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenReturn(mockSearchResponse(List.of(hit)));

        LogSearchQuery query = new LogSearchQuery();
        query.setQuery("database");

        byte[] exported = analyticsService.exportLogs(query);

        assertNotNull(exported);
        String json = new String(exported);
        assertTrue(json.contains("Database failure"));
        assertTrue(json.contains("ERROR"));
        assertTrue(json.contains("DatabaseService"));
    }

    @Test
    void testSearchLogs_IOException_ThrowsRuntimeException() throws Exception {
        when(elasticsearchClient.search((SearchRequest) any(), any(Class.class)))
                .thenThrow(new IOException("Elasticsearch down"));

        LogSearchQuery query = new LogSearchQuery();
        query.setQuery("error");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> analyticsService.searchLogs(query));
        assertTrue(ex.getMessage().contains("Failed to search logs"));
    }
}
