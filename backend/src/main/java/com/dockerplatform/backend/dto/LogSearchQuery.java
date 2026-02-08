package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogSearchQuery {
    private String query;
    private String startDate;
    private String endDate;
    private List<String> levels;
    private Integer size = 50;
    private Integer from = 0;
}
