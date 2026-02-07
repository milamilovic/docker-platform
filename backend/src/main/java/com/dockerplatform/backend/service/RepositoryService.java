package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.CreateRepositoryDto;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositoryUpdateDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

@Service
public class RepositoryService {

    @Autowired
    RepositoryRepo repositoryRepo;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    UserRepo userRepo;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Pageable convertToNativePageable(Pageable pageable) {
        Sort nativeSort = Sort.unsorted();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                String column = camelToSnake(property);
                nativeSort = nativeSort.and(Sort.by(order.getDirection(), column));
            }
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), nativeSort);
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Cacheable(value = "myRepositories",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#search ?: 'null') + '_' + #visibility",
            unless = "#result == null")
    public CacheablePage<RepositoryDto> getMyRepositories(Pageable pageable, String search, String visibility) {
        User currentUser = getCurrentUser();

        // Convert to native column names for the query
        Pageable nativePageable = convertToNativePageable(pageable);

        Page<Repository> repositoryPage = repositoryRepo.findByOwnerWithFilters(
                currentUser.getId(),
                search,
                visibility != null ? visibility : "all",
                nativePageable
        );

        Page<RepositoryDto> dtoPage = repositoryPage.map(RepositoryDto::toResponseDto);
        return new CacheablePage<>(
                new ArrayList<>(dtoPage.getContent()),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
    }

    @Cacheable(value = "officialRepositories",
            key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#search ?: 'null')",
            unless = "#result == null")
    public CacheablePage<RepositoryDto> getOfficialRepositories(Pageable pageable, String search) {
        // Convert to native column names for the query
        Pageable nativePageable = convertToNativePageable(pageable);

        Page<Repository> repositoryPage = repositoryRepo.findByIsOfficialWithFilters(
                search,
                nativePageable
        );

        Page<RepositoryDto> dtoPage = repositoryPage.map(RepositoryDto::toResponseDto);
        return new CacheablePage<>(
                new ArrayList<>(dtoPage.getContent()),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
    }

    @Cacheable(value = "myOfficialRepositories",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#search ?: 'null')",
            unless = "#result == null")
    public CacheablePage<RepositoryDto> getMyOfficialRepositories(Pageable pageable, String search) {
        if (!isAdmin()) {
            throw new AccessDeniedException("Only admins can manage official repositories");
        }

        User currentUser = getCurrentUser();

        // Convert to native column names for the query
        Pageable nativePageable = convertToNativePageable(pageable);

        Page<Repository> repositoryPage = repositoryRepo.findByOwnerAndIsOfficialWithFilters(
                currentUser.getId(),
                search,
                nativePageable
        );

        Page<RepositoryDto> dtoPage = repositoryPage.map(RepositoryDto::toResponseDto);
        return new CacheablePage<>(
                new ArrayList<>(dtoPage.getContent()),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements()
        );
    }

    @Cacheable(value = "repository", key = "#id", unless = "#result == null")
    public RepositoryDto getRepositoryById(UUID id) {
        Repository repository = repositoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check if user has access to this repository
        User currentUser = getCurrentUser();
        if (!repository.isPublic() &&
                !repository.getOwner().getId().equals(currentUser.getId()) &&
                !isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }

        return RepositoryDto.toResponseDto(repository);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "myRepositories", allEntries = true),
            @CacheEvict(value = "officialRepositories", allEntries = true, condition = "#dto.isOfficial()"),
            @CacheEvict(value = "myOfficialRepositories", allEntries = true, condition = "#dto.isOfficial()")
    })
    public RepositoryDto createRepository(CreateRepositoryDto dto) {
        User currentUser = getCurrentUser();

        // Check if it's an official repository
        if (dto.isOfficial()) {
            if (!isAdmin()) {
                throw new AccessDeniedException("Only admins can create official repositories");
            }

            // Check if official repository with this name already exists
            if (repositoryRepo.existsByName(dto.getName())) {
                throw new RuntimeException("Official repository with this name already exists");
            }
        } else {
            // Check if user already has repository with this name
            if (repositoryRepo.existsByOwnerAndName(currentUser, dto.getName())) {
                throw new RuntimeException("You already have a repository with this name");
            }
        }

        Repository repository = new Repository();
        repository.setName(dto.getName());
        repository.setOwner(currentUser);
        repository.setDescription(dto.getDescription());
        repository.setPublic(dto.isPublic());
        repository.setOfficial(dto.isOfficial() && isAdmin());
        long now = System.currentTimeMillis();
        repository.setCreatedAt(now);
        repository.setModifiedAt(now);
        repository.setNumberOfPulls(0);
        repository.setNumberOfStars(0);
        repository.setTags(new HashSet<>());

        repository = repositoryRepo.save(repository);
        return RepositoryDto.toResponseDto(repository);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "repository", key = "#id"),
            @CacheEvict(value = "myRepositories", allEntries = true),
            @CacheEvict(value = "officialRepositories", allEntries = true),
            @CacheEvict(value = "myOfficialRepositories", allEntries = true)
    })
    public RepositoryDto updateRepository(UUID id, RepositoryUpdateDto dto) {
        Repository repository = repositoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check if user is owner
        User currentUser = getCurrentUser();
        if (!repository.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (dto.getDescription() != null) {
            repository.setDescription(dto.getDescription());
        }

        if (dto.getIsPublic() != null) {
            if(repository.isOfficial() && isAdmin() && dto.getIsPublic() == Boolean.TRUE) {
                throw new IllegalArgumentException("Official repositories can not be private!");
            }
            repository.setPublic(dto.getIsPublic());
        }

        long now = System.currentTimeMillis();
        repository.setModifiedAt(now);

        repository = repositoryRepo.save(repository);
        return RepositoryDto.toResponseDto(repository);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "repository", key = "#id"),
            @CacheEvict(value = "myRepositories", allEntries = true),
            @CacheEvict(value = "officialRepositories", allEntries = true),
            @CacheEvict(value = "myOfficialRepositories", allEntries = true),
            @CacheEvict(value = "repositoryTags", allEntries = true)
    })
    public void deleteRepository(UUID id) {
        Repository repository = repositoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check if user is owner or admin
        User currentUser = getCurrentUser();
        if (!repository.getOwner().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }

        // Delete all tags first
        tagRepo.deleteByRepository(repository);

        // Delete repository
        repositoryRepo.delete(repository);
    }
}