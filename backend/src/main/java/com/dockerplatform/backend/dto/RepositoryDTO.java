package com.dockerplatform.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class RepositoryDTO {
    private UUID id;
    private String name;
    private String description;

    private String ownerUsername;

    private int numberOfPulls;
    private int numberOfStars;

    private boolean isOfficial;

    private Long createdAt;
    private Long modifiedAt;
}
