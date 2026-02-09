package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StarRequestDto {
    private UUID userId;
    private UUID repositoryId;
    private boolean starred;
}