package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.StarRequestDto;
import com.dockerplatform.backend.service.StarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.dockerplatform.backend.dto.CacheablePage;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stars")
public class StarController {

    @Autowired
    private StarService starService;

    @PostMapping
    public ResponseEntity<Void> setStar(@RequestBody StarRequestDto req) {
        starService.setStar(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CacheablePage<RepositoryDto>> getStarredRepositoriesByUser(
            @PathVariable UUID userId,
            Pageable pageable,
            @RequestParam(required = false) String search
    ) {
        CacheablePage<RepositoryDto> page = starService.getStarredRepositoriesByUser(userId, pageable, search);
        return ResponseEntity.ok(page);
    }
}
