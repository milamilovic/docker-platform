package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("novi_user");
        userDto.setPassword("sifra123");
        userDto.setEmail("novi@example.com");

        String jsonRequest = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("novi_user")))
                .andExpect(jsonPath("$.email", is("novi@example.com")));
    }

    @Test
    void testRegisterUser_DuplicateUsername_ReturnsBadRequest() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("stari_user");
        existingUser.setPassword("lozinka");
        existingUser.setEmail("stari@example.com");
        userRepo.save(existingUser);

        UserDto userDto = new UserDto();
        userDto.setUsername("stari_user");
        userDto.setPassword("nebitno");
        userDto.setEmail("drugi@example.com");

        String jsonRequest = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterUser_EmptyBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}