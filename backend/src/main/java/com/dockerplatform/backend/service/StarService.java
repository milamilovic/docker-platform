package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.StarRequestDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Star;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.StarRepo;
import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class StarService {

    @Autowired
    private StarRepo starRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Caching(evict = {
            @CacheEvict(value = "officialRepositories", allEntries = true),
            @CacheEvict(value = "myRepositories", allEntries = true),
            @CacheEvict(value = "myOfficialRepositories", allEntries = true),
            @CacheEvict(value = "userStarredRepo", allEntries = true)
    })
    @Transactional
    public void setStar(StarRequestDto req) {
        UUID userId = req.getUserId();
        UUID repoId = req.getRepositoryId();

        if (req.isStarred()) {
            // STAR (idempotentno)
            if (starRepository.findByUserIdAndRepositoryId(userId, repoId).isPresent()) {
                return;
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Repository repo = repositoryRepo.findById(repoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));

            Star star = new Star();
            star.setUser(user);
            star.setRepository(repo);
            repo.setNumberOfStars(repo.getNumberOfStars() + 1);
            try {
                starRepository.save(star);
                repositoryRepo.save(repo);
            } catch (DataIntegrityViolationException ignored) {
                // race condition (ako postoji unique constraint user_id+repository_id)
            }

        } else {
            // UNSTAR (idempotentno)
            Repository repo = repositoryRepo.findById(repoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));
            starRepository.deleteByUserIdAndRepositoryId(userId, repoId);
            repo.setNumberOfStars(repo.getNumberOfStars() - 1);
            repositoryRepo.save(repo);
        }
    }

    @Transactional(readOnly = true)
    public CacheablePage<RepositoryDto> getStarredRepositoriesByUser(UUID userId, Pageable pageable, String search) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Pageable nativePageable = convertToNativePageable(pageable);

        Page<Repository> repositoryPage = starRepository.findStarredRepositoriesByUser(
                userId,
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

    private String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
