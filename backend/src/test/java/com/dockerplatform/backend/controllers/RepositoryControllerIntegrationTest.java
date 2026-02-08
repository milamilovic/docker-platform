package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Autowired
    private UserRepo userRepo;

    private User testUser;
    private User adminUser;
    private Repository testRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
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

        // Create admin user
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("admin123");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepo.save(adminUser);

        // Create test repository
        testRepository = new Repository();
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
        testRepository = repositoryRepo.save(testRepository);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetMyRepositories_Success() throws Exception {
        mockMvc.perform(get("/repositories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("visibility", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", is("test-repo")))
                .andExpect(jsonPath("$.content[0].description", is("Test repository")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetMyRepositories_WithSearch() throws Exception {
        mockMvc.perform(get("/repositories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "test")
                        .param("visibility", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].name", containsString("test")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetRepositoryById_Success() throws Exception {
        mockMvc.perform(get("/repositories/{id}", testRepository.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testRepository.getId().toString())))
                .andExpect(jsonPath("$.name", is("test-repo")))
                .andExpect(jsonPath("$.description", is("Test repository")))
                .andExpect(jsonPath("$.isPublic", is(true)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testCreateRepository_Success() throws Exception {
        String requestBody = """
                {
                    "name": "new-repo",
                    "description": "New test repository",
                    "isPublic": true,
                    "isOfficial": false
                }
                """;

        mockMvc.perform(post("/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("new-repo")))
                .andExpect(jsonPath("$.description", is("New test repository")))
                .andExpect(jsonPath("$.isPublic", is(true)))
                .andExpect(jsonPath("$.isOfficial", is(false)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testCreateRepository_DuplicateName_Fails() throws Exception {
        String requestBody = """
                {
                    "name": "test-repo",
                    "description": "Duplicate repository",
                    "isPublic": true,
                    "isOfficial": false
                }
                """;

        mockMvc.perform(post("/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("You already have a repository with this name")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testUpdateRepository_Success() throws Exception {
        String requestBody = """
                {
                    "description": "Updated description",
                    "isPublic": false
                }
                """;

        mockMvc.perform(put("/repositories/{id}", testRepository.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.isPublic", is(false)));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"REGULAR"})
    void testUpdateRepository_NotOwner_Fails() throws Exception {
        // Create the other user in database
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(UserRole.REGULAR);
        userRepo.save(otherUser);

        String requestBody = """
                {
                    "description": "Hacked description"
                }
                """;

        mockMvc.perform(put("/repositories/{id}", testRepository.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Access denied")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testDeleteRepository_Success() throws Exception {
        mockMvc.perform(delete("/repositories/{id}", testRepository.getId()))
                .andExpect(status().isNoContent());

        // Verify repository is deleted
        mockMvc.perform(get("/repositories/{id}", testRepository.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testDeleteRepository_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/repositories/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetRepositoryById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/repositories/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateOfficialRepository_AsAdmin_Success() throws Exception {
        String requestBody = """
                {
                    "name": "official-repo",
                    "description": "Official repository",
                    "isPublic": true,
                    "isOfficial": true
                }
                """;

        mockMvc.perform(post("/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("official-repo")))
                .andExpect(jsonPath("$.isOfficial", is(true)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testCreateOfficialRepository_AsRegularUser_Fails() throws Exception {
        String requestBody = """
                {
                    "name": "official-repo",
                    "description": "Official repository",
                    "isPublic": true,
                    "isOfficial": true
                }
                """;

        mockMvc.perform(post("/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admins can create official repositories")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetOfficialRepositories_Success() throws Exception {
        // Create an official repository
        Repository officialRepo = new Repository();
        officialRepo.setName("official-test");
        officialRepo.setDescription("Official test repository");
        officialRepo.setOwner(adminUser);
        officialRepo.setPublic(true);
        officialRepo.setOfficial(true);
        officialRepo.setCreatedAt(System.currentTimeMillis());
        officialRepo.setModifiedAt(System.currentTimeMillis());
        officialRepo.setNumberOfPulls(0);
        officialRepo.setNumberOfStars(0);
        officialRepo.setTags(new HashSet<>());
        repositoryRepo.save(officialRepo);

        mockMvc.perform(get("/repositories/official")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[?(@.name == 'official-test')].isOfficial", hasItem(true)));
    }

    @Test
    void testGetRepositories_Unauthorized() throws Exception {
        mockMvc.perform(get("/repositories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"REGULAR"})
    void testGetMyOfficialRepositories_AsRegularUser_Fails() throws Exception {
        mockMvc.perform(get("/repositories/my-official")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }
}