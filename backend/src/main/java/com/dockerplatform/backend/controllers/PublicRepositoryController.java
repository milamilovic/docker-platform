package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.service.PublicRepositoryService;
import com.dockerplatform.backend.service.PublicSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/repositories")
public class PublicRepositoryController {

    @Autowired
    PublicRepositoryService publicRepositoryService;

    @Autowired
    PublicSearchService publicSearchService;

    @GetMapping("/top-pulled")
    public Page<RepositorySearchDTO> getTopPulledRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicRepositoryService.findTopPulled(page, size);
    }

    @GetMapping("/top-starred")
    public Page<RepositorySearchDTO> getTopStarredRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicRepositoryService.findTopStarred(page, size);
    }

    @GetMapping("/search")
    public Page<RepositorySearchDTO> getSearchedRepositories(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return publicSearchService.search(q, page, size);
    }

    @GetMapping("/official")
    public ResponseEntity<CacheablePage<RepositoryDto>> getOfficialRepositories(
            Pageable pageable,
            @RequestParam() String badge,
            @RequestParam(required = false) String search) {

        CacheablePage<RepositoryDto> page = publicRepositoryService.findFilteredOfficial(badge, search, pageable);
        return ResponseEntity.ok(page);
    }
}
