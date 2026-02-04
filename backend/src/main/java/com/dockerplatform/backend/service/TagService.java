package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.TagDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<TagDto> getTagsByRepository(UUID repositoryId) {
        Repository repository = repositoryRepo.findById(repositoryId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check if user has access to this repository
        User currentUser = getCurrentUser();
        if (!repository.isPublic() &&
                !repository.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<Tag> tags = tagRepo.findByRepository(repository);
        return tags.stream()
                .map(TagDto::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepo.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        Repository repository = tag.getRepository();

        // Check if user is owner
        User currentUser = getCurrentUser();
        if (!repository.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        tagRepo.delete(tag);
    }
}
