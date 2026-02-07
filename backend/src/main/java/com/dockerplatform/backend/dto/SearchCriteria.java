package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String repo;
    private String owner;
    private String description;
    private List<String> general;
    private Boolean isOfficial;
    private Boolean isVerified;
    private Boolean isSponsored;
}
