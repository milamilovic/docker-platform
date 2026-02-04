package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.TagDto;
import com.dockerplatform.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    TagService tagService;

    @GetMapping("/{repositoryId}/tags")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<List<TagDto>> getTagsByRepository(@PathVariable UUID repositoryId) {
        List<TagDto> tags = tagService.getTagsByRepository(repositoryId);
        return ResponseEntity.ok(tags);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
