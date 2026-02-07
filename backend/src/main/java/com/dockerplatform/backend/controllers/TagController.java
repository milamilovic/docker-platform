package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.TagDto;
import com.dockerplatform.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    TagService tagService;

    @GetMapping("/{repositoryId}/tags")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<CacheablePage<TagDto>> getTagsByRepository(
            @PathVariable UUID repositoryId,
            Pageable pageable,
            @RequestParam(required = false) String search) {

        CacheablePage<TagDto> page = tagService.getTagsByRepository(repositoryId, pageable, search);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}