package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RegistryNotification;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistryTagSyncServiceTest {

    @Mock private RepositoryRepo repositoryRepo;
    @Mock private TagRepo tagRepo;
    @Mock private CacheService cacheService;

    @InjectMocks
    private RegistryTagSyncService service;

    private Repository repo;

    @BeforeEach
    void setUp() {
        repo = new Repository();
        repo.setName("repo");
    }

    @Test
    void handleNotification_nullPayload_noInteractions() {
        service.handleNotification(null);
        verifyNoInteractions(repositoryRepo, tagRepo);
    }

    @Test
    void handleNotification_nullEvents_noInteractions() {
        RegistryNotification n = new RegistryNotification();
        n.events = null;

        service.handleNotification(n);
        verifyNoInteractions(repositoryRepo, tagRepo);
    }

    @Test
    void handleNotification_ignoresNonPushNonManifestActions() {
        RegistryNotification n = new RegistryNotification();
        RegistryNotification.Event e = new RegistryNotification.Event();
        e.action = "pull";
        e.target = new RegistryNotification.Target();
        e.target.repository = "owner/repo";
        e.target.tag = "v1";
        e.target.digest = "sha256:abc";
        e.target.length = 123L;

        n.events = java.util.List.of(e);

        service.handleNotification(n);
        verifyNoInteractions(repositoryRepo, tagRepo);
    }

    @Test
    void handleNotification_validEvent_callsUpdateTagPath() {
        // real updateTag will run; we just ensure that with existing repo we save
        when(repositoryRepo.findByOwnerUsernameAndName("owner", "repo"))
                .thenReturn(Optional.of(repo));
        when(tagRepo.findByRepositoryAndName(repo, "v1")).thenReturn(Optional.empty());

        RegistryNotification n = new RegistryNotification();
        RegistryNotification.Event e = new RegistryNotification.Event();
        e.action = "push";
        e.target = new RegistryNotification.Target();
        e.target.repository = "owner/repo";
        e.target.tag = "v1";
        e.target.digest = "sha256:abc";
        e.target.length = 100L;
        n.events = java.util.List.of(e);

        service.handleNotification(n);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepo).save(captor.capture());

        Tag saved = captor.getValue();
        assertEquals(repo, saved.getRepository());
        assertEquals("v1", saved.getName());
        assertEquals("sha256:abc", saved.getDigest());
        assertEquals(100L, saved.getSize());
        assertTrue(saved.getCreatedAt() > 0);
        assertTrue(saved.getPushedAt() > 0);
    }

    @Test
    void updateTag_invalidRepoFullName_noInteractions() {
        service.updateTag("badname", "v1", "sha256:abc", 10L, 111L);
        verifyNoInteractions(repositoryRepo, tagRepo);
    }

    @Test
    void updateTag_repoNotFound_noSave() {
        when(repositoryRepo.findByOwnerUsernameAndName("owner", "repo"))
                .thenReturn(Optional.empty());

        service.updateTag("owner/repo", "v1", "sha256:abc", 10L, 111L);

        verify(repositoryRepo).findByOwnerUsernameAndName("owner", "repo");
        verifyNoInteractions(tagRepo);
    }

    @Test
    void updateTag_existingTag_updatesFields_andKeepsCreatedAt() {
        when(repositoryRepo.findByOwnerUsernameAndName("owner", "repo"))
                .thenReturn(Optional.of(repo));

        Tag existing = new Tag();
        existing.setRepository(repo);
        existing.setName("v1");
        existing.setCreatedAt(50L);

        when(tagRepo.findByRepositoryAndName(repo, "v1"))
                .thenReturn(Optional.of(existing));

        service.updateTag("owner/repo", "v1", "sha256:new", 0L, 999L);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepo).save(captor.capture());

        Tag saved = captor.getValue();
        assertEquals(50L, saved.getCreatedAt());
        assertEquals(999L, saved.getPushedAt());
        assertEquals("sha256:new", saved.getDigest());
        assertEquals(0L, saved.getSize());
    }

    @Test
    void updateTag_newTag_createdAtSet_sizeNullHandled() {
        when(repositoryRepo.findByOwnerUsernameAndName("owner", "repo"))
                .thenReturn(Optional.of(repo));
        when(tagRepo.findByRepositoryAndName(repo, "v2"))
                .thenReturn(Optional.empty());

        service.updateTag("owner/repo", "v2", "sha256:x", null, 1234L);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepo).save(captor.capture());

        Tag saved = captor.getValue();
        assertEquals("v2", saved.getName());
        assertEquals("sha256:x", saved.getDigest());
        assertEquals(0L, saved.getSize());
        assertEquals(1234L, saved.getPushedAt());
        assertEquals(1234L, saved.getCreatedAt());
    }
}
