package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String repo;
    private String owner;
    private String description;
    private List<String> general;
    private Set<BadgeType> badges;
}
