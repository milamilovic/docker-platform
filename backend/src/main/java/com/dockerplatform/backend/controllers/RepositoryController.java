package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.CreateRepositoryDto;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositoryUpdateDto;
import com.dockerplatform.backend.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    @Autowired
    RepositoryService repositoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<List<RepositoryDto>> getMyRepositories() {
        List<RepositoryDto> repositories = repositoryService.getMyRepositories();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/official")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<List<RepositoryDto>> getOfficialRepositories() {
        List<RepositoryDto> repositories = repositoryService.getOfficialRepositories();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/my-official")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RepositoryDto>> getMyOfficialRepositories() {
        List<RepositoryDto> repositories = repositoryService.getMyOfficialRepositories();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<RepositoryDto> getRepositoryById(@PathVariable UUID id) {
        RepositoryDto repository = repositoryService.getRepositoryById(id);
        return ResponseEntity.ok(repository);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<RepositoryDto> createRepository(@RequestBody CreateRepositoryDto dto) {
        RepositoryDto repository = repositoryService.createRepository(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<RepositoryDto> updateRepository(
            @PathVariable UUID id,
            @RequestBody RepositoryUpdateDto dto) {
        RepositoryDto repository = repositoryService.updateRepository(id, dto);
        return ResponseEntity.ok(repository);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR', 'ADMIN')")
    public ResponseEntity<Void> deleteRepository(@PathVariable UUID id) {
        repositoryService.deleteRepository(id);
        return ResponseEntity.noContent().build();
    }
}
