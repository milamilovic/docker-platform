package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.ChangePasswordRequest;
import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.REGULAR);

        userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("rawPassword");
        userDto.setEmail("test@example.com");
    }

    @Test
    void testFindByUsername_UserExists() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindByUsername_UserNotFound() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        User result = userService.findByUsername("unknown");

        assertNull(result);
    }

    @Test
    void testRegister_Success() {
        when(userRepo.findByUsername(userDto.getUsername())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        Optional<User> result = userService.register(userDto, UserRole.REGULAR);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        when(userRepo.findByUsername(userDto.getUsername())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.register(userDto, UserRole.REGULAR);

        assertFalse(result.isPresent());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("testuser", "oldPassword", "newPassword");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bCryptPasswordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        boolean result = userService.changePassword(request);

        assertTrue(result);
        verify(userRepo, times(1)).save(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("testuser", "wrongOldPassword", "newPassword");

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(bCryptPasswordEncoder.matches("wrongOldPassword", testUser.getPassword())).thenReturn(false);

        boolean result = userService.changePassword(request);

        assertFalse(result);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest("nonexistent", "oldPassword", "newPassword");
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        boolean result = userService.changePassword(request);

        assertFalse(result);
        verify(userRepo, never()).save(any(User.class));
    }
}