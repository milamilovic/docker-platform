package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.CacheablePage;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.BadgeType;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.PublicRepositoryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class PublicRepositoryServiceTest {

    @Mock
    private PublicRepositoryRepo repositoryRepo;

    @InjectMocks
    private PublicRepositoryService repositoryService;

    private List<RepositorySearchDTO> createDummyRepoDtos() {
        List<RepositorySearchDTO> list = new ArrayList<>();

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setName("Repo1");
        repo1.setDescription("Description 1");
        repo1.setOwnerUsername("User1");
        repo1.setNumberOfPulls(10);
        repo1.setNumberOfStars(5);
        repo1.setOfficial(false);
        repo1.setCreatedAt(System.currentTimeMillis());
        repo1.setModifiedAt(System.currentTimeMillis());
        repo1.setBadge(BadgeType.SPONSORED_OSS);
        list.add(RepositorySearchDTO.from(repo1));

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setName("Repo2");
        repo2.setDescription("Description 2");
        repo2.setOwnerUsername("User2");
        repo2.setNumberOfPulls(20);
        repo2.setNumberOfStars(10);
        repo2.setOfficial(true);
        repo2.setCreatedAt(System.currentTimeMillis());
        repo2.setModifiedAt(System.currentTimeMillis());
        repo2.setBadge(BadgeType.VERIFIED_PUBLISHER);
        list.add(RepositorySearchDTO.from(repo2));

        Repository repo3 = new Repository();
        repo3.setId(UUID.randomUUID());
        repo3.setName("Repo3");
        repo3.setDescription("Description 3");
        repo3.setOwnerUsername("User3");
        repo3.setNumberOfPulls(30);
        repo3.setNumberOfStars(15);
        repo3.setOfficial(false);
        repo3.setCreatedAt(System.currentTimeMillis());
        repo3.setModifiedAt(System.currentTimeMillis());
        repo3.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        list.add(RepositorySearchDTO.from(repo3));

        return list;
    }

    private List<Repository> createDummyRepos() {
        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("Alice");
        owner.setPassword("password123");
        owner.setEmail("alice@example.com");
        owner.setRole(UserRole.ADMIN);

        List<Repository> list = new ArrayList<>();

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setOwner(owner);
        repo1.setName("Repo1");
        repo1.setDescription("Description 1");
        repo1.setOwnerUsername("User1");
        repo1.setNumberOfPulls(10);
        repo1.setNumberOfStars(5);
        repo1.setOfficial(false);
        repo1.setCreatedAt(System.currentTimeMillis());
        repo1.setModifiedAt(System.currentTimeMillis());
        repo1.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        list.add(repo1);

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setOwner(owner);
        repo2.setName("Repo2");
        repo2.setDescription("Description 2");
        repo2.setOwnerUsername("User2");
        repo2.setNumberOfPulls(20);
        repo2.setNumberOfStars(10);
        repo2.setOfficial(true);
        repo2.setCreatedAt(System.currentTimeMillis());
        repo2.setModifiedAt(System.currentTimeMillis());
        repo2.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        list.add(repo2);

        Repository repo3 = new Repository();
        repo3.setId(UUID.randomUUID());
        repo3.setOwner(owner);
        repo3.setName("Repo3");
        repo3.setDescription("Description 3");
        repo3.setOwnerUsername("User3");
        repo3.setNumberOfPulls(30);
        repo3.setNumberOfStars(15);
        repo3.setOfficial(false);
        repo3.setCreatedAt(System.currentTimeMillis());
        repo3.setModifiedAt(System.currentTimeMillis());
        repo3.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        list.add(repo3);

        return list;
    }

    @Test
    void testFindTopPulled() {
        int page = 0;
        int size = 5;

        List<RepositorySearchDTO> dummyRepos = createDummyRepoDtos();
        Page<RepositorySearchDTO> pageResult = new PageImpl<>(dummyRepos);

        when(repositoryRepo.findTopPulled(PageRequest.of(page, size)))
                .thenReturn(pageResult);

        Page<RepositorySearchDTO> result = repositoryService.findTopPulled(page, size);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Repo1", result.getContent().get(0).getName());

        verify(repositoryRepo).findTopPulled(PageRequest.of(page, size));
    }

    @Test
    void testFindTopPulled_empty() {
        int page = 0;
        int size = 5;

        Page<RepositorySearchDTO> emptyPage = Page.empty();

        when(repositoryRepo.findTopPulled(PageRequest.of(page, size)))
                .thenReturn(emptyPage);

        Page<RepositorySearchDTO> result = repositoryService.findTopPulled(page, size);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(repositoryRepo).findTopPulled(PageRequest.of(page, size));
    }

    @Test
    void testFindTopStarred() {
        int page = 0;
        int size = 5;

        List<RepositorySearchDTO> dummyRepos = createDummyRepoDtos();
        Page<RepositorySearchDTO> pageResult = new PageImpl<>(dummyRepos);

        when(repositoryRepo.findTopStarred(PageRequest.of(page, size)))
                .thenReturn(pageResult);

        Page<RepositorySearchDTO> result = repositoryService.findTopStarred(page, size);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Repo1", result.getContent().get(0).getName());

        verify(repositoryRepo).findTopStarred(PageRequest.of(page, size));
    }

    @Test
    void testFindTopStarred_empty() {
        int page = 0;
        int size = 5;

        Page<RepositorySearchDTO> emptyPage = Page.empty();

        when(repositoryRepo.findTopStarred(PageRequest.of(page, size)))
                .thenReturn(emptyPage);

        Page<RepositorySearchDTO> result = repositoryService.findTopStarred(page, size);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(repositoryRepo).findTopStarred(PageRequest.of(page, size));
    }

    @Test
    void testFindFiltered_Official() {
        String badge = "DOCKER_OFFICIAL_IMAGE";
        String search = "Repo";
        Pageable pageable = PageRequest.of(0, 5);

        List<Repository> dummyRepos = createDummyRepos();
        Page<Repository> repoPage = new PageImpl<>(dummyRepos);

        when(repositoryRepo.findFilteredOfficial(badge, search, pageable))
                .thenReturn(repoPage);

        CacheablePage<RepositoryDto> result = repositoryService.findFilteredOfficial(badge, search, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals("Repo1", result.getContent().get(0).getName());

        verify(repositoryRepo).findFilteredOfficial(badge, search, pageable);
    }

}
