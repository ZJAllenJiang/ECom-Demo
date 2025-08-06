package org.allen.service;

import org.allen.entity.User;
import org.allen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password456");
        testUser2.setFirstName("Test2");
        testUser2.setLastName("User2");
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        String hashedPassword = "hashedPassword123";
        when(passwordService.hashPassword(anyString())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(hashedPassword, result.getPassword());
        assertNotNull(result.getCreatedAt());
        verify(passwordService).hashPassword("password123");
        verify(userRepository).save(testUser);
    }

    @Test
    void testCreateUser_WithNullPassword() {
        // Arrange
        testUser.setPassword(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testCreateUser_WithEmptyPassword() {
        // Arrange
        testUser.setPassword("");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, testUser2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById_Exists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_NotExists() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void testGetUserByUsername_Exists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetUserByEmail_Exists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(1L, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_NotExists() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        User result = userService.updateUser(999L, testUser);

        // Assert
        assertNull(result);
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testExistsByUsername_True() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void testExistsByUsername_False() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void testExistsByEmail_True() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void testExistsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
} 