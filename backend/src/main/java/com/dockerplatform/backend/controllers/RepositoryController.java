package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.RepositoryDTO;
import com.dockerplatform.backend.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    @Autowired
    RepositoryService repositoryService;

    @GetMapping("/top-pulled")
    public Page<RepositoryDTO> getTopPulledRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return repositoryService.findTopPulled(page, size);
    }

    @GetMapping("/top-starred")
    public Page<RepositoryDTO> getTopStarredRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return repositoryService.findTopStarred(page, size);
    }
}
