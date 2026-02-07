package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogSearchResponse {
    private List<LogEntry> hits;
    private Long total;
    private Long took;
}
