package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RepositoryDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private String ownerUsername;
    private String description;
    private Long createdAt;
    private Long modifiedAt;
    private int numberOfPulls;
    private int numberOfStars;
    @JsonProperty("isPublic")
    private boolean isPublic;
    @JsonProperty("isOfficial")
    private boolean isOfficial;
    private BadgeType badge;

    public static RepositoryDto toResponseDto(Repository repository) {
        RepositoryDto dto = new RepositoryDto();
        dto.setId(repository.getId());
        dto.setName(repository.getName());
        dto.setOwnerId(repository.getOwner().getId());
        dto.setOwnerUsername(repository.getOwner().getUsername());
        dto.setDescription(repository.getDescription());
        dto.setCreatedAt(repository.getCreatedAt());
        dto.setModifiedAt(repository.getModifiedAt());
        dto.setNumberOfPulls(repository.getNumberOfPulls());
        dto.setNumberOfStars(repository.getNumberOfStars());
        dto.setPublic(repository.isPublic());
        dto.setOfficial(repository.isOfficial());
        dto.setBadge(repository.getBadge());
        return dto;
    }
}
