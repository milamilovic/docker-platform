package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RepositoryDTO;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

    @Autowired
    RepositoryRepo repositoryRepo;

    public Page<RepositoryDTO> findTopPulled(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repositoryRepo.findTopPulled(pageable);
    }

    public Page<RepositoryDTO> findTopStarred(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repositoryRepo.findTopStarred(pageable);
    }
}
