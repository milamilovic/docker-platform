package com.dockerplatform.backend.dto;

import com.dockerplatform.backend.models.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TagDto {
    private UUID id;
    private String name;
    private String digest;
    private long size;
    private Long createdAt;
    private Long pushedAt;
    private UUID repositoryId;

    public static TagDto toDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDigest(tag.getDigest());
        dto.setSize(tag.getSize());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setPushedAt(tag.getPushedAt());
        dto.setRepositoryId(tag.getRepository().getId());
        return dto;
    }
}
