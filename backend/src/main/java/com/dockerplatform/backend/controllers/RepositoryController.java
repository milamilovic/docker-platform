package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.CreateRepositoryDto;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositoryUpdateDto;
import com.dockerplatform.backend.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    @Autowired
    RepositoryService repositoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<CacheablePage<RepositoryDto>> getMyRepositories(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String visibility) {

        CacheablePage<RepositoryDto> page = repositoryService.getMyRepositories(pageable, search, visibility);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/official")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<CacheablePage<RepositoryDto>> getOfficialRepositories(
            Pageable pageable,
            @RequestParam(required = false) String search) {

        CacheablePage<RepositoryDto> page = repositoryService.getOfficialRepositories(pageable, search);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/my-official")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CacheablePage<RepositoryDto>> getMyOfficialRepositories(
            Pageable pageable,
            @RequestParam(required = false) String search) {

        CacheablePage<RepositoryDto> page = repositoryService.getMyOfficialRepositories(pageable, search);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<RepositoryDto> getRepositoryById(@PathVariable UUID id) {
        try {
            RepositoryDto repository = repositoryService.getRepositoryById(id);
            return ResponseEntity.ok(repository);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<?> createRepository(@RequestBody CreateRepositoryDto dto) {
        try {
            RepositoryDto repository = repositoryService.createRepository(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(repository);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already have a repository") ||
                    e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<?> updateRepository(
            @PathVariable UUID id,
            @RequestBody RepositoryUpdateDto dto) {
        try {
            RepositoryDto repository = repositoryService.updateRepository(id, dto);
            return ResponseEntity.ok(repository);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<Void> deleteRepository(@PathVariable UUID id) {
        try {
            repositoryService.deleteRepository(id);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}