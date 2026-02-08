package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RepositoryCardDto {
    private String name;
    private String description;
    private String isPublic;
    private Boolean isOfficial = false;
}
