package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.TagDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TagService {

    @Autowired
    TagRepo tagRepo;

    @Autowired
    RepositoryRepo repositoryRepo;

    @Autowired
    UserRepo userRepo;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Converts JPA property names (camelCase) to database column names (snake_case)
     * for native queries
     */
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

    /**
     * Converts camelCase to snake_case
     * e.g., pushedAt -> pushed_at, createdAt -> created_at
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public Page<TagDto> getTagsByRepository(UUID repositoryId, Pageable pageable, String search) {
        Repository repository = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check if user has access to this repository
        User currentUser = getCurrentUser();
        if (!repository.isPublic() &&
                !repository.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        // Convert to native column names for the query
        Pageable nativePageable = convertToNativePageable(pageable);

        Page<Tag> tagPage = tagRepo.findByRepositoryWithFilters(
                repositoryId,
                search,
                nativePageable
        );

        return tagPage.map(TagDto::toDto);
    }

    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepo.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        Repository repository = tag.getRepository();

        // Check if user is owner
        User currentUser = getCurrentUser();
        if (!repository.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        tagRepo.delete(tag);
    }
}