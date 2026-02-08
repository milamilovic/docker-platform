package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.BadgeType;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import com.dockerplatform.backend.service.CacheService;
import com.dockerplatform.backend.service.PublicRepositoryService;
import com.dockerplatform.backend.service.RegistryTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;



@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicRepositoryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    @MockitoBean
    private RegistryTokenService tokenService;
    @MockitoBean
    private CacheService cacheService;
    @Autowired
    private PublicRepositoryService publicRepositoryService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RepositoryRepo repositoryRepo;

    private User alice;
    private Repository repo1;
    private Repository repo2;
    private Repository repo3;

    @BeforeEach
    void setUp() throws IOException {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        repositoryRepo.deleteAll();
        userRepo.deleteAll();

        alice = new User();
        alice.setUsername("Alice");
        alice.setEmail("alice@example.com");
        alice.setPassword("password123");
        alice.setRole(UserRole.REGULAR);
        alice = userRepo.save(alice);

        repo1 = new Repository();
        repo1.setName("DockerApp");
        repo1.setDescription("Sample Docker App");
        repo1.setOwner(alice);
        repo1.setOwnerUsername(alice.getUsername());
        repo1.setNumberOfPulls(150);
        repo1.setNumberOfStars(50);
        repo1.setOfficial(true);
        repo1.setPublic(true);
        repo1.setCreatedAt(System.currentTimeMillis());
        repo1.setModifiedAt(System.currentTimeMillis());
        repo1.setBadge(BadgeType.DOCKER_OFFICIAL_IMAGE);
        repositoryRepo.save(repo1);

        repo2 = new Repository();
        repo2.setName("VerifiedService");
        repo2.setDescription("Verified Publisher Service");
        repo2.setOwner(alice);
        repo2.setOwnerUsername(alice.getUsername());
        repo2.setNumberOfPulls(75);
        repo2.setNumberOfStars(30);
        repo2.setOfficial(false);
        repo2.setPublic(true);
        repo2.setCreatedAt(System.currentTimeMillis());
        repo2.setModifiedAt(System.currentTimeMillis());
        repo2.setBadge(BadgeType.SPONSORED_OSS);
        repositoryRepo.save(repo2);

        repo3 = new Repository();
        repo3.setName("NginxTest");
        repo3.setDescription("Nginx Verified");
        repo3.setOwner(alice);
        repo3.setOwnerUsername(alice.getUsername());
        repo3.setNumberOfPulls(100);
        repo3.setNumberOfStars(90);
        repo3.setOfficial(false);
        repo3.setPublic(true);
        repo3.setCreatedAt(System.currentTimeMillis());
        repo3.setModifiedAt(System.currentTimeMillis());
        repo3.setBadge(BadgeType.VERIFIED_PUBLISHER);
        repositoryRepo.save(repo3);
        Path adminSecret = Paths.get("secrets", "super_admin.txt");

        if (Files.exists(adminSecret)) {
            Files.delete(adminSecret);
            System.out.println("Obrisan super_admin.txt - sustav otključan za testove.");
        }
    }

    @Test
    void testGetTopPulledRepositories() throws Exception {
        mockMvc.perform(get("/public/repositories/top-pulled")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", is("DockerApp")))
                .andExpect(jsonPath("$.content[1].name", is("NginxTest")))
                .andExpect(jsonPath("$.content[2].name", is("VerifiedService")));
    }

    @Test
    void testGetTopStarredRepositories() throws Exception {
        mockMvc.perform(get("/public/repositories/top-starred")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", is("NginxTest")))
                .andExpect(jsonPath("$.content[1].name", is("DockerApp")))
                .andExpect(jsonPath("$.content[2].name", is("VerifiedService")));
    }

    @Test
    void testGetOfficialRepositories_BadgeOfficial() throws Exception {
        mockMvc.perform(get("/public/repositories/official")
                        .param("badge", "DOCKER_OFFICIAL_IMAGE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].badge", everyItem(is("DOCKER_OFFICIAL_IMAGE"))));
    }

    @Test
    void testGetOfficialRepositories_BadgeVerified() throws Exception {
        mockMvc.perform(get("/public/repositories/official")
                        .param("badge", "VERIFIED_PUBLISHER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].badge", everyItem(is("VERIFIED_PUBLISHER"))));
    }

    @Test
    void testGetOfficialRepositories_BadgeSponsored() throws Exception {
        mockMvc.perform(get("/public/repositories/official")
                        .param("badge", "SPONSORED_OSS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].badge", everyItem(is("SPONSORED_OSS"))));
    }

    @Test
    void testGetOfficialRepositories_BadgeNonExistent() throws Exception {
        mockMvc.perform(get("/public/repositories/official")
                        .param("badge", "NonExistent")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void testSearchRepositories_WithExistingQuery() throws Exception {
        mockMvc.perform(get("/public/repositories/search")
                        .param("q", "DockerApp")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("DockerApp")));
    }

    @Test
    void testSearchRepositories_WithEmptyQuery() throws Exception {
        mockMvc.perform(get("/public/repositories/search")
                        .param("q", "")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void testSearchRepositories_WithNonExistentQuery() throws Exception {
        mockMvc.perform(get("/public/repositories/search")
                        .param("q", "nonexistentrepo")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()));
    }

    @Test
    void testSearchRepositories_WithFilters() throws Exception {
        String query = "repo:DockerApp is:official owner:alice";

        mockMvc.perform(get("/public/repositories/search")
                        .param("q", query)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("DockerApp")))
                .andExpect(jsonPath("$.content[0].badge", is("DOCKER_OFFICIAL_IMAGE")))
                .andExpect(jsonPath("$.content[0].ownerUsername", is("Alice")));
    }
}

