package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RepositoryUpdateDto {
    private String description;
    private Boolean isPublic;
}
