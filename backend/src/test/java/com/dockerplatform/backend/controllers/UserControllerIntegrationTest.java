package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.UserRepo;
import com.dockerplatform.backend.service.CacheService;
import com.dockerplatform.backend.service.RegistryTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockitoBean
    private RegistryTokenService tokenService;
    @MockitoBean
    private CacheService cacheService;
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
//        userRepo.deleteAll();

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        Path adminSecret = Paths.get("secrets", "super_admin.txt");

        if (Files.exists(adminSecret)) {
            Files.delete(adminSecret);
            System.out.println("Obrisan super_admin.txt - sustav otključan za testove.");
        }
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("novikorisnik");
        userDto.setEmail("novi@example.com");
        userDto.setPassword("sifra123");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("novikorisnik")))
                .andExpect(jsonPath("$.email", is("novi@example.com")));
    }

    @Test
    void testRegisterUser_DuplicateUsername_Fails() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("postojeći");
        userDto.setEmail("prvi@example.com");
        userDto.setPassword("sifra123");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void testRegisterAdmin_Success() throws Exception {
        UserDto adminDto = new UserDto();
        adminDto.setUsername("noviAdmin");
        adminDto.setEmail("admin@test.com");

        mockMvc.perform(post("/user/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("noviAdmin")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

}