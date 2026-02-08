package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RegistryNotification;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class RegistryTagSyncService {

    @Autowired
    private RepositoryRepo repositoryRepo;
    @Autowired
    private TagRepo tagRepo;

    @Transactional
    public void handleNotification(RegistryNotification payload) {
        if (payload == null || payload.events == null) {
            System.out.println("[REGISTRY] payload or events = null");
            return;
        }

        long now = Instant.now().toEpochMilli();

        System.out.println("[REGISTRY] notification received, events=" + payload.events.size());

        for (var e : payload.events) {
            if (e == null) {
                System.out.println("[REGISTRY] event = null, skipping");
                continue;
            }

            if (e.action == null || e.target == null) {
                System.out.println("[REGISTRY] missing action or target, skipping");
                continue;
            }

            if (e.target.repository == null) {
                System.out.println("[REGISTRY] target.repository = null, skipping");
                continue;
            }

            String action = e.action.toLowerCase();
            if (!action.contains("push") && !action.contains("manifest")) {
                System.out.println("[REGISTRY] skipping action=" + e.action);
                continue;
            }

            if (e.target.tag == null || e.target.tag.isBlank()) {
                System.out.println("[REGISTRY] missing tag for repo=" + e.target.repository);
                continue;
            }

            if (e.target.digest == null || e.target.digest.isBlank()) {
                System.out.println("[REGISTRY] missing digest for repo=" + e.target.repository
                        + " tag=" + e.target.tag);
                continue;
            }

            System.out.println(
                    "[REGISTRY] PROCESS repo=" + e.target.repository
                            + " tag=" + e.target.tag
                            + " digest=" + e.target.digest
                            + " size=" + e.target.length
            );

            upsertSingleTag(
                    e.target.repository,
                    e.target.tag,
                    e.target.digest,
                    e.target.length,
                    now
            );
        }
    }


    @Transactional
    public void upsertSingleTag(String repositoryFullName,
                                String tagName,
                                String digest,
                                Long length,
                                long now) {

        System.out.println("[REGISTRY] UPSERT start: " + repositoryFullName + ":" + tagName);

        String[] parts = repositoryFullName.split("/", 2);
        if (parts.length != 2) {
            System.out.println("[REGISTRY] invalid repo format: " + repositoryFullName);
            return;
        }

        String ownerUsername = parts[0];
        String repoName = parts[1];

        Repository repo = repositoryRepo
                .findByOwnerUsernameAndName(ownerUsername, repoName)
                .orElse(null);

        if (repo == null) {
            System.out.println("[REGISTRY] repo NOT FOUND in DB: " + repositoryFullName);
            return;
        }

        Tag tag = tagRepo.findByRepositoryAndName(repo, tagName).orElse(null);

        if (tag == null) {
            System.out.println("[REGISTRY] creating NEW tag: " + repositoryFullName + ":" + tagName);
            tag = new Tag();
            tag.setRepository(repo);
            tag.setName(tagName);
            tag.setCreatedAt(now);
        } else {
            System.out.println("[REGISTRY] updating EXISTING tag: " + repositoryFullName + ":" + tagName);
        }

        tag.setDigest(digest);
        tag.setSize(length != null && length > 0 ? length : 0);
        tag.setPushedAt(now);

        tagRepo.save(tag);

        System.out.println(
                "[REGISTRY] UPSERT DONE: "
                        + repositoryFullName + ":" + tagName
                        + " digest=" + digest
                        + " size=" + tag.getSize()
        );
    }
}