package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.repositories.PublicRepositoryRepo;
import com.dockerplatform.backend.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PublicRepositoryService {

    @Autowired
    PublicRepositoryRepo publicRepositoryRepo;

    public Page<RepositorySearchDTO> findTopPulled(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publicRepositoryRepo.findTopPulled(pageable);
    }

    public Page<RepositorySearchDTO> findTopStarred(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publicRepositoryRepo.findTopStarred(pageable);
    }

    public CacheablePage<RepositoryDto> findFilteredOfficial(String badge, String search, Pageable pageable) {

        Pageable nativePageable = PageUtils.convertToNativePageable(pageable);

        Page<Repository> repositoryPage = publicRepositoryRepo.findFilteredOfficial(badge, search, nativePageable);

        Page<RepositoryDto> dtoPage = repositoryPage.map(RepositoryDto::toResponseDto);

        return new CacheablePage<>(
                new ArrayList<>(dtoPage.getContent()),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
    }

}
