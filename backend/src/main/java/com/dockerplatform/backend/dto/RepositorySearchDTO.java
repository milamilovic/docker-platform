package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepositorySearchDTO {
    private UUID id;
    private String name;
    private String description;

    private String ownerUsername;

    private int numberOfPulls;
    private int numberOfStars;

    private boolean isOfficial;

    private Long createdAt;
    private Long modifiedAt;
    private BadgeType badge;

    public static RepositorySearchDTO from(Repository repo) {
        RepositorySearchDTO dto = new RepositorySearchDTO();
        dto.setId(repo.getId());
        dto.setName(repo.getName());
        dto.setDescription(repo.getDescription());
        dto.setOwnerUsername(repo.getOwnerUsername());
        dto.setNumberOfPulls(repo.getNumberOfPulls());
        dto.setNumberOfStars(repo.getNumberOfStars());
        dto.setOfficial(repo.isOfficial());
        dto.setCreatedAt(repo.getCreatedAt());
        dto.setModifiedAt(repo.getCreatedAt());
        dto.setBadge(repo.getBadge());
        return dto;
    }
}
