package com.dockerplatform.backend.service;


import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.dto.SearchCriteria;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import com.dockerplatform.backend.repositories.PublicRepositoryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicSearchServiceTest {
    @Mock
    private PublicRepositoryRepo repositoryRepo;

    @InjectMocks
    private PublicSearchService searchService;

    private List<Repository> createDummyRepos() {
        List<Repository> list = new ArrayList<>();

        Repository repo1 = new Repository();
        repo1.setId(UUID.randomUUID());
        repo1.setName("DockerApp");
        repo1.setDescription("Sample Docker App");
        repo1.setOwnerUsername("Alice");
        repo1.setNumberOfPulls(100);
        repo1.setNumberOfStars(50);
        repo1.setOfficial(true);
        repo1.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        repo1.setCreatedAt(System.currentTimeMillis());
        repo1.setModifiedAt(System.currentTimeMillis());
        list.add(repo1);

        Repository repo2 = new Repository();
        repo2.setId(UUID.randomUUID());
        repo2.setName("VerifiedService");
        repo2.setDescription("Verified Publisher Service");
        repo2.setOwnerUsername("Bob");
        repo2.setNumberOfPulls(75);
        repo2.setNumberOfStars(30);
        repo2.setOfficial(false);
        repo2.setBadge(BadgeType.VERIFIED_PUBLISHER);
        repo2.setCreatedAt(System.currentTimeMillis());
        repo2.setModifiedAt(System.currentTimeMillis());
        list.add(repo2);

        return list;
    }

    @Test
    void testSearch_withQuery_returnsResults() {
        String query = "owner:Alice DockerApp";
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        List<Repository> dummyRepos = createDummyRepos();
        Page<Repository> repoPage = new PageImpl<>(dummyRepos);

        when(repositoryRepo.findAll(ArgumentMatchers.<Specification<Repository>>any(), eq(pageable)))
                .thenReturn(repoPage);

        Page<RepositorySearchDTO> result = searchService.search(query, page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("DockerApp", result.getContent().get(0).getName());
        assertEquals("VerifiedService", result.getContent().get(1).getName());

        verify(repositoryRepo).findAll(ArgumentMatchers.<Specification<Repository>>any(), eq(pageable));
    }

    @Test
    void testSearch_withQuery_returnsEmptyList() {
        String query = "owner:NonExistingUser";
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Page<Repository> emptyPage = Page.empty();
        when(repositoryRepo.findAll(ArgumentMatchers.<Specification<Repository>>any(), eq(pageable)))
                .thenReturn(emptyPage);

        Page<RepositorySearchDTO> result = searchService.search(query, page, size);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());

        verify(repositoryRepo).findAll(ArgumentMatchers.<Specification<Repository>>any(), eq(pageable));
    }

    @Test
    void testParse_withOwnerRepoBadgeAndGeneralToken() {
        String query = "owner:Alice repo:DockerApp is:verified extraToken";

        SearchCriteria criteria = searchService.parse(query);

        assertEquals("Alice", criteria.getOwner());
        assertEquals("DockerApp", criteria.getRepo());
        assertNull(criteria.getDescription());
        assertTrue(criteria.getBadges().contains(BadgeType.VERIFIED_PUBLISHER));
        assertEquals(List.of("extraToken"), criteria.getGeneral());
    }

    @Test
    void testParse_withOnlyGeneralTokens() {
        String query = "foo bar baz";

        SearchCriteria criteria = searchService.parse(query);

        assertNull(criteria.getOwner());
        assertNull(criteria.getRepo());
        assertNull(criteria.getDescription());
        assertTrue(criteria.getBadges().isEmpty());
        assertEquals(List.of("foo", "bar", "baz"), criteria.getGeneral());
    }

    @Test
    void testParse_withMultipleBadges() {
        String query = "is:official is:sponsored";

        SearchCriteria criteria = searchService.parse(query);

        assertTrue(criteria.getBadges().contains(BadgeType.DOCKER_OFFICIAL_IMAGE));
        assertTrue(criteria.getBadges().contains(BadgeType.SPONSORED_OSS));
    }

    @Test
    void testParse_withNullOrEmptyQuery() {
        SearchCriteria criteria1 = searchService.parse(null);
        SearchCriteria criteria2 = searchService.parse("");
        SearchCriteria criteria3 = searchService.parse("   ");

        assertTrue(criteria1.getGeneral().isEmpty());
        assertTrue(criteria1.getBadges().isEmpty());

        assertTrue(criteria2.getGeneral().isEmpty());
        assertTrue(criteria2.getBadges().isEmpty());

        assertTrue(criteria3.getGeneral().isEmpty());
        assertTrue(criteria3.getBadges().isEmpty());
    }

    @Test
    void testBuildSpecification_returnsNonNull() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setRepo("DockerApp");
        criteria.setOwner("Alice");
        criteria.setDescription("Sample");
        criteria.setGeneral(List.of("token1", "token2"));
        criteria.setBadges(Set.of(BadgeType.VERIFIED_PUBLISHER));

        Specification<Repository> spec = searchService.buildSpecification(criteria);

        assertNotNull(spec); 
    }



}