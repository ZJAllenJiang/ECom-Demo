package org.allen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allen.dto.UserRegistrationDTO;
import org.allen.entity.User;
import org.allen.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserRegistrationDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());

        testUserDTO = new UserRegistrationDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // Arrange
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).existsByUsername("testuser");
        verify(userService).existsByEmail("test@example.com");
        verify(userService).createUser(any(User.class));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() throws Exception {
        // Arrange
        when(userService.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isConflict());

        verify(userService).existsByUsername("testuser");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testCreateUser_EmailAlreadyExists() throws Exception {
        // Arrange
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isConflict());

        verify(userService).existsByUsername("testuser");
        verify(userService).existsByEmail("test@example.com");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testCreateUser_ValidationError_EmptyUsername() throws Exception {
        // Arrange
        testUserDTO.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").exists());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testCreateUser_ValidationError_InvalidEmail() throws Exception {
        // Arrange
        testUserDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testCreateUser_ValidationError_ShortPassword() throws Exception {
        // Arrange
        testUserDTO.setPassword("123");

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(999L), any(User.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(eq(999L), any(User.class));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }

    @Test
    void testCheckUsernameExists_True() throws Exception {
        // Arrange
        when(userService.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/check-username/testuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).existsByUsername("testuser");
    }

    @Test
    void testCheckUsernameExists_False() throws Exception {
        // Arrange
        when(userService.existsByUsername("nonexistent")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/check-username/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService).existsByUsername("nonexistent");
    }

    @Test
    void testCheckEmailExists_True() throws Exception {
        // Arrange
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/check-email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).existsByEmail("test@example.com");
    }

    @Test
    void testCheckEmailExists_False() throws Exception {
        // Arrange
        when(userService.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/check-email/nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService).existsByEmail("nonexistent@example.com");
    }
} 