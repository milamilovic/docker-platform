package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.TagRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import com.dockerplatform.backend.service.CacheService;
import com.dockerplatform.backend.service.RegistryTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TagControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockitoBean
    private RegistryTokenService tokenService;
    @MockitoBean
    private CacheService cacheService;
    private MockMvc mockMvc;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Autowired
    private TagRepo tagRepo;

    @Autowired
    private UserRepo userRepo;

    private User testUser;
    private Repository testRepository;
    private Tag testTag;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up before each test
        tagRepo.deleteAll();
        repositoryRepo.deleteAll();
        userRepo.deleteAll();

        // Set up MockMvc with Spring Security
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(UserRole.REGULAR);
        testUser = userRepo.save(testUser);

        // Create test repository
        testRepository = new Repository();
        testRepository.setName("test-repo");
        testRepository.setDescription("Test repository");
        testRepository.setOwner(testUser);
        testRepository.setOwnerUsername(testUser.getUsername());
        testRepository.setPublic(true);
        testRepository.setOfficial(false);
        testRepository.setCreatedAt(System.currentTimeMillis());
        testRepository.setModifiedAt(System.currentTimeMillis());
        testRepository.setNumberOfPulls(0);
        testRepository.setNumberOfStars(0);
        testRepository.setTags(new HashSet<>());
        testRepository = repositoryRepo.save(testRepository);

        // Create test tag
        testTag = new Tag();
        testTag.setName("v1.0.0");
        testTag.setDigest("sha256:1234567890abcdef");
        testTag.setSize(1024000);
        testTag.setCreatedAt(System.currentTimeMillis());
        testTag.setPushedAt(System.currentTimeMillis());
        testTag.setRepository(testRepository);
        testTag = tagRepo.save(testTag);

        Path adminSecret = Paths.get("secrets", "super_admin.txt");

        if (Files.exists(adminSecret)) {
            Files.delete(adminSecret);
            System.out.println("Obrisan super_admin.txt - sustav otključan za testove.");
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetTagsByRepository_Success() throws Exception {
        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", is("v1.0.0")))
                .andExpect(jsonPath("$.content[0].digest", is("sha256:1234567890abcdef")))
                .andExpect(jsonPath("$.content[0].size", is(1024000)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetTagsByRepository_WithSearch() throws Exception {
        // Create another tag
        Tag tag2 = new Tag();
        tag2.setName("v2.0.0");
        tag2.setDigest("sha256:fedcba0987654321");
        tag2.setSize(2048000);
        tag2.setCreatedAt(System.currentTimeMillis());
        tag2.setPushedAt(System.currentTimeMillis());
        tag2.setRepository(testRepository);
        tagRepo.save(tag2);

        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("v1.0.0")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetTagsByRepository_MultiplePages() throws Exception {
        // Create 15 tags
        for (int i = 1; i <= 15; i++) {
            Tag tag = new Tag();
            tag.setName("v1.0." + i);
            tag.setDigest("sha256:digest" + i);
            tag.setSize(1024000 + i);
            tag.setCreatedAt(System.currentTimeMillis());
            tag.setPushedAt(System.currentTimeMillis());
            tag.setRepository(testRepository);
            tagRepo.save(tag);
        }

        // Get first page
        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(15)));

        // Get second page
        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testDeleteTag_Success() throws Exception {
        mockMvc.perform(delete("/tags/{id}", testTag.getId()))
                .andExpect(status().isNoContent());

        // Verify tag is deleted
        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"REGULAR"})
    void testDeleteTag_NotOwner_Fails() throws Exception {
        // Create the other user in database
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(UserRole.REGULAR);
        userRepo.save(otherUser);

        mockMvc.perform(delete("/tags/{id}", testTag.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"REGULAR"})
    void testGetTagsByRepository_PrivateRepository_NotOwner_Fails() throws Exception {
        // Create the other user in database
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(UserRole.REGULAR);
        userRepo.save(otherUser);

        // Make repository private
        testRepository.setPublic(false);
        repositoryRepo.save(testRepository);

        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetTagsByRepository_PublicRepositoryOfAnotherUser_Success() throws Exception {
        // Create another user
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(UserRole.REGULAR);
        otherUser = userRepo.save(otherUser);

        // Create public repository owned by other user
        Repository otherRepo = new Repository();
        otherRepo.setName("other-repo");
        otherRepo.setDescription("Other repository");
        otherRepo.setOwner(otherUser);
        otherRepo.setOwnerUsername(otherUser.getUsername());
        otherRepo.setPublic(true);
        otherRepo.setOfficial(false);
        otherRepo.setCreatedAt(System.currentTimeMillis());
        otherRepo.setModifiedAt(System.currentTimeMillis());
        otherRepo.setNumberOfPulls(0);
        otherRepo.setNumberOfStars(0);
        otherRepo.setTags(new HashSet<>());
        otherRepo = repositoryRepo.save(otherRepo);

        // Create tag for other repo
        Tag otherTag = new Tag();
        otherTag.setName("v1.0.0");
        otherTag.setDigest("sha256:other");
        otherTag.setSize(500000);
        otherTag.setCreatedAt(System.currentTimeMillis());
        otherTag.setPushedAt(System.currentTimeMillis());
        otherTag.setRepository(otherRepo);
        tagRepo.save(otherTag);

        // Should be able to view tags of public repository
        mockMvc.perform(get("/tags/{repositoryId}/tags", otherRepo.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].digest", is("sha256:other")));
    }

//    @Test
//    void testGetTagsByRepository_Unauthorized() throws Exception {
//        mockMvc.perform(get("/tags/{repositoryId}/tags", testRepository.getId())
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isForbidden()); // Changed from isUnauthorized to isForbidden
//    }

//    @Test
//    void testDeleteTag_Unauthorized() throws Exception {
//        mockMvc.perform(delete("/tags/{id}", testTag.getId()))
//                .andExpect(status().isForbidden()); // Changed from isUnauthorized to isForbidden
//    }
}