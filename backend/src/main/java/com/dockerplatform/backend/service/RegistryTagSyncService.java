package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RegistryNotification;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class RegistryTagSyncService {

    @Autowired
    private RepositoryRepo repositoryRepo;
    @Autowired
    private TagRepo tagRepo;

    @Autowired
    private CacheService cacheService;

    @Transactional
    public void handleNotification(RegistryNotification payload) {
        if (payload == null || payload.events == null) {
            return;
        }

        long now = Instant.now().toEpochMilli();

        for (var e : payload.events) {
            if (e == null || e.action == null || e.target == null) {
                continue;
            }

            if (e.target.repository == null) {
                continue;
            }

            String action = e.action.toLowerCase();
            if (!action.contains("push") && !action.contains("manifest")) {
                continue;
            }

            if (e.target.tag == null || e.target.tag.isBlank()) {
                continue;
            }

            if (e.target.digest == null || e.target.digest.isBlank()) {
                continue;
            }

            updateTag(
                    e.target.repository,
                    e.target.tag,
                    e.target.digest,
                    e.target.length,
                    now
            );
        }
    }

    @Transactional
    public void updateTag(String repositoryFullName,
                                String tagName,
                                String digest,
                                Long length,
                                long now) {

        String[] parts = repositoryFullName.split("/", 2);
        if (parts.length != 2) {
            return;
        }

        String ownerUsername = parts[0];
        String repoName = parts[1];

        Repository repo = repositoryRepo
                .findByOwnerUsernameAndName(ownerUsername, repoName)
                .orElse(null);

        if (repo == null) {
            return;
        }

        Tag tag = tagRepo.findByRepositoryAndName(repo, tagName).orElse(null);

        if (tag == null) {
            tag = new Tag();
            tag.setRepository(repo);
            tag.setName(tagName);
            tag.setCreatedAt(now);
        }

        tag.setDigest(digest);
        tag.setSize(length != null && length > 0 ? length : 0);
        tag.setPushedAt(now);

        tagRepo.save(tag);
        cacheService.clearAllCaches();
    }
}