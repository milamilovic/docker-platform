package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.DockerPlatformBackendApplication;
import com.dockerplatform.backend.dto.LogSearchQuery;
import com.dockerplatform.backend.dto.LogSearchResponse;
import com.dockerplatform.backend.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(DockerPlatformBackendApplication.class);

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/logs")
    public ResponseEntity<LogSearchResponse> searchLogs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(defaultValue = "0") Integer from) {

        LogSearchQuery searchQuery = new LogSearchQuery();
        searchQuery.setQuery(query);
        searchQuery.setStartDate(startDate);
        searchQuery.setEndDate(endDate);
        searchQuery.setLevels(levels);
        searchQuery.setSize(size);
        searchQuery.setFrom(from);

        LogSearchResponse response = analyticsService.searchLogs(searchQuery);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs(@RequestBody LogSearchQuery query) {
        byte[] exportData = analyticsService.exportLogs(query);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=logs-export.json")
                .header("Content-Type", "application/json")
                .body(exportData);
    }
}
