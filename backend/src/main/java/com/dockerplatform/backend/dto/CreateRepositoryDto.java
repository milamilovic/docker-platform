package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.Repository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateRepositoryDto {
    private String name;
    private String description;
    private boolean isPublic;
    private boolean isOfficial;

    public static CreateRepositoryDto toResponseDto(Repository repository) {
        CreateRepositoryDto dto = new CreateRepositoryDto();
        dto.setName(repository.getName());
        dto.setDescription(repository.getDescription());
        dto.setPublic(repository.isPublic());
        dto.setOfficial(repository.isOfficial());
        return dto;
    }
}
