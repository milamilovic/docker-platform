package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.TagDto;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.models.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepo tagRepo;

    @Mock
    private RepositoryRepo repositoryRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TagService tagService;

    private User testUser;
    private Repository testRepository;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

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

        // Setup test tag
        testTag = new Tag();
        testTag.setId(UUID.randomUUID());
        testTag.setName("v1.0.0");
        testTag.setDigest("sha256:1234567890abcdef");
        testTag.setSize(1024000);
        testTag.setCreatedAt(System.currentTimeMillis());
        testTag.setPushedAt(System.currentTimeMillis());
        testTag.setRepository(testRepository);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetTagsByRepository_PublicRepository_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> tagPage = new PageImpl<>(Collections.singletonList(testTag));
        when(tagRepo.findByRepositoryWithFilters(
                eq(testRepository.getId()),
                any(),
                any(Pageable.class)
        )).thenReturn(tagPage);

        // Act
        Page<TagDto> result = tagService.getTagsByRepository(testRepository.getId(), pageable, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("v1.0.0", result.getContent().get(0).getName());
        assertEquals("sha256:1234567890abcdef", result.getContent().get(0).getDigest());
        verify(tagRepo, times(1)).findByRepositoryWithFilters(
                eq(testRepository.getId()),
                any(),
                any(Pageable.class)
        );
    }

    @Test
    void testGetTagsByRepository_WithSearch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> tagPage = new PageImpl<>(Collections.singletonList(testTag));
        when(tagRepo.findByRepositoryWithFilters(
                eq(testRepository.getId()),
                eq("v1.0"),
                any(Pageable.class)
        )).thenReturn(tagPage);

        // Act
        Page<TagDto> result = tagService.getTagsByRepository(testRepository.getId(), pageable, "v1.0");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tagRepo, times(1)).findByRepositoryWithFilters(
                eq(testRepository.getId()),
                eq("v1.0"),
                any(Pageable.class)
        );
    }

    @Test
    void testGetTagsByRepository_RepositoryNotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(repositoryRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tagService.getTagsByRepository(nonExistentId, pageable, null);
        });

        assertEquals("Repository not found", exception.getMessage());
        verify(tagRepo, never()).findByRepositoryWithFilters(any(), any(), any());
    }

    @Test
    void testGetTagsByRepository_PrivateRepository_NotOwner_ThrowsException() {
        // Arrange
        testRepository.setPublic(false);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otheruser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("otheruser");
        when(userRepo.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        when(repositoryRepo.findById(testRepository.getId())).thenReturn(Optional.of(testRepository));

        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tagService.getTagsByRepository(testRepository.getId(), pageable, null);
        });

        assertEquals("Access denied", exception.getMessage());
        verify(tagRepo, never()).findByRepositoryWithFilters(any(), any(), any());
    }

    @Test
    void testDeleteTag_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(tagRepo.findById(testTag.getId())).thenReturn(Optional.of(testTag));

        // Act
        tagService.deleteTag(testTag.getId());

        // Assert
        verify(tagRepo, times(1)).delete(testTag);
    }

    @Test
    void testDeleteTag_NotFound_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(tagRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tagService.deleteTag(nonExistentId);
        });

        assertEquals("Tag not found", exception.getMessage());
        verify(tagRepo, never()).delete(any(Tag.class));
    }

    @Test
    void testDeleteTag_NotOwner_ThrowsException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setUsername("otheruser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("otheruser");
        when(userRepo.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        when(tagRepo.findById(testTag.getId())).thenReturn(Optional.of(testTag));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tagService.deleteTag(testTag.getId());
        });

        assertEquals("Access denied", exception.getMessage());
        verify(tagRepo, never()).delete(any(Tag.class));
    }
}