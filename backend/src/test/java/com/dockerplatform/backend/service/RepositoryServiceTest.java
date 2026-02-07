package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.CreateRepositoryDto;
import com.dockerplatform.backend.dto.RepositoryDto;
import com.dockerplatform.backend.dto.RepositoryUpdateDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryRepo repositoryRepo;

    @Mock
    private TagRepo tagRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RepositoryService repositoryService;

    private User testUser;
    private User adminUser;
    private Repository testRepository;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");

        // Setup test repository
        testRepository = new Repository();
        testRepository.setId(UUID.randomUUID());
        testRepository.setName("test-repo");
        testRepository.setDescription("Test repository");
        testRepository.setOwner(testUser);
        testRepository.setPublic(true);
        testRepository.setOfficial(false);
        testRepository.setCreatedAt(System.currentTimeMillis());
        testRepository.setModifiedAt(System.currentTimeMillis());
        testRepository.setNumberOfPulls(0);
        testRepository.setNumberOfStars(0);
        testRepository.setTags(new HashSet<>());

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupAuthentication(String username, User user, boolean isAdmin) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));

        if (isAdmin) {
            lenient().when(authentication.getAuthorities()).thenAnswer(invocation ->
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        } else {
            lenient().when(authentication.getAuthorities()).thenAnswer(invocation ->
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_REGULAR"))
            );
        }
    }

    @Test
    void testGetMyRepositories_Success() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> repositoryPage = new PageImpl<>(Collections.singletonList(testRepository));
        when(repositoryRepo.findByOwnerWithFilters(
                eq(testUser.getId()),
                any(),
                eq("all"),
                any(Pageable.class)
        )).thenReturn(repositoryPage);

        // Act
        Page<RepositoryDto> result = repositoryService.getMyRepositories(pageable, null, "all");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test-repo", result.getContent().get(0).getName());
        verify(repositoryRepo, times(1)).findByOwnerWithFilters(
                eq(testUser.getId()),
                any(),
                eq("all"),
                any(Pageable.class)
        );
    }

    @Test
    void testGetOfficialRepositories_Success() {
        // Arrange
        Repository officialRepo = new Repository();
        officialRepo.setId(UUID.randomUUID());
        officialRepo.setName("official-repo");
        officialRepo.setDescription("Official repository");
        officialRepo.setOwner(adminUser);
        officialRepo.setPublic(true);
        officialRepo.setOfficial(true);
        officialRepo.setCreatedAt(System.currentTimeMillis());
        officialRepo.setModifiedAt(System.currentTimeMillis());
        officialRepo.setNumberOfPulls(100);
        officialRepo.setNumberOfStars(50);
        officialRepo.setTags(new HashSet<>());

        Pageable pageable = PageRequest.of(0, 10);
        Page<Repository> repositoryPage = new PageImpl<>(Collections.singletonList(officialRepo));
        when(repositoryRepo.findByIsOfficialWithFilters(any(), any(Pageable.class)))
                .thenReturn(repositoryPage);

        // Act
        Page<RepositoryDto> result = repositoryService.getOfficialRepositories(pageable, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isOfficial());
        verify(repositoryRepo, times(1)).findByIsOfficialWithFilters(any(), any(Pageable.class));
    }

    @Test
    void testCreateRepository_Success() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        CreateRepositoryDto dto = new CreateRepositoryDto();
        dto.setName("new-repo");
        dto.setDescription("New test repository");
        dto.setPublic(true);
        dto.setOfficial(false);

        when(repositoryRepo.existsByOwnerAndName(testUser, "new-repo")).thenReturn(false);
        when(repositoryRepo.save(any(Repository.class))).thenAnswer(invocation -> {
            Repository repo = invocation.getArgument(0);
            repo.setId(UUID.randomUUID());
            return repo;
        });

        // Act
        RepositoryDto result = repositoryService.createRepository(dto);

        // Assert
        assertNotNull(result);
        assertEquals("new-repo", result.getName());
        assertEquals("New test repository", result.getDescription());
        assertTrue(result.isPublic());
        assertFalse(result.isOfficial());
        verify(repositoryRepo, times(1)).save(any(Repository.class));
    }

    @Test
    void testCreateRepository_DuplicateName_ThrowsException() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        CreateRepositoryDto dto = new CreateRepositoryDto();
        dto.setName("existing-repo");
        dto.setDescription("Description");
        dto.setPublic(true);
        dto.setOfficial(false);

        when(repositoryRepo.existsByOwnerAndName(testUser, "existing-repo")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repositoryService.createRepository(dto);
        });

        assertEquals("You already have a repository with this name", exception.getMessage());
        verify(repositoryRepo, never()).save(any(Repository.class));
    }

    @Test
    void testCreateOfficialRepository_NonAdmin_ThrowsException() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        CreateRepositoryDto dto = new CreateRepositoryDto();
        dto.setName("official-repo");
        dto.setDescription("Official repository");
        dto.setPublic(true);
        dto.setOfficial(true);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            repositoryService.createRepository(dto);
        });

        assertEquals("Only admins can create official repositories", exception.getMessage());
        verify(repositoryRepo, never()).save(any(Repository.class));
    }

    @Test
    void testUpdateRepository_Success() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));
        when(repositoryRepo.save(any(Repository.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepositoryUpdateDto dto = new RepositoryUpdateDto();
        dto.setDescription("Updated description");
        dto.setIsPublic(false);

        // Act
        RepositoryDto result = repositoryService.updateRepository(testRepository.getId(), dto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.isPublic());
        verify(repositoryRepo, times(1)).save(any(Repository.class));
    }

    @Test
    void testUpdateRepository_NotOwner_ThrowsException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otheruser");

        setupAuthentication("otheruser", otherUser, false);

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        RepositoryUpdateDto dto = new RepositoryUpdateDto();
        dto.setDescription("Hacked description");

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            repositoryService.updateRepository(testRepository.getId(), dto);
        });

        assertEquals("Access denied", exception.getMessage());
        verify(repositoryRepo, never()).save(any(Repository.class));
    }

    @Test
    void testDeleteRepository_Success() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        // Act
        repositoryService.deleteRepository(testRepository.getId());

        // Assert
        verify(tagRepo, times(1)).deleteByRepository(testRepository);
        verify(repositoryRepo, times(1)).delete(testRepository);
    }

    @Test
    void testDeleteRepository_NotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repositoryRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repositoryService.deleteRepository(nonExistentId);
        });

        assertEquals("Repository not found", exception.getMessage());
        verify(repositoryRepo, never()).delete(any(Repository.class));
    }

    @Test
    void testGetRepositoryById_PublicRepository_Success() {
        // Arrange
        setupAuthentication("testuser", testUser, false);

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        // Act
        RepositoryDto result = repositoryService.getRepositoryById(testRepository.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testRepository.getName(), result.getName());
    }

    @Test
    void testGetRepositoryById_PrivateRepository_NotOwner_ThrowsException() {
        // Arrange
        testRepository.setPublic(false);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otheruser");

        setupAuthentication("otheruser", otherUser, false);

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            repositoryService.getRepositoryById(testRepository.getId());
        });

        assertEquals("Access denied", exception.getMessage());
    }
}