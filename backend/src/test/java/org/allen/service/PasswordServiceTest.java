package org.allen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void testHashPassword_Success() {
        // Arrange
        String rawPassword = "MySecurePassword123";

        // Act
        String hashedPassword = passwordService.hashPassword(rawPassword);

        // Assert
        assertNotNull(hashedPassword);
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertTrue(hashedPassword.length() > 50);
    }

    @Test
    void testHashPassword_DifferentPasswords() {
        // Arrange
        String password1 = "Password123";
        String password2 = "Password456";

        // Act
        String hashed1 = passwordService.hashPassword(password1);
        String hashed2 = passwordService.hashPassword(password2);

        // Assert
        assertNotEquals(hashed1, hashed2);
    }

    @Test
    void testHashPassword_SamePasswordDifferentHashes() {
        // Arrange
        String password = "SamePassword123";

        // Act
        String hashed1 = passwordService.hashPassword(password);
        String hashed2 = passwordService.hashPassword(password);

        // Assert
        assertNotEquals(hashed1, hashed2); // BCrypt generates different salts
    }

    @Test
    void testHashPassword_NullPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> passwordService.hashPassword(null)
        );
        
        assertEquals("rawPassword cannot be null", exception.getMessage());
    }

    @Test
    void testHashPassword_EmptyPassword() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> passwordService.hashPassword("")
        );
        
        assertEquals("rawPassword cannot be empty", exception.getMessage());
    }

    @Test
    void testMatches_ValidPassword() {
        // Arrange
        String rawPassword = "TestPassword123";
        String hashedPassword = passwordService.hashPassword(rawPassword);

        // Act
        boolean result = passwordService.matches(rawPassword, hashedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void testMatches_InvalidPassword() {
        // Arrange
        String correctPassword = "CorrectPassword123";
        String wrongPassword = "WrongPassword123";
        String hashedPassword = passwordService.hashPassword(correctPassword);

        // Act
        boolean result = passwordService.matches(wrongPassword, hashedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testMatches_EmptyPassword() {
        // Arrange
        String hashedPassword = passwordService.hashPassword("SomePassword123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> passwordService.matches("", hashedPassword)
        );
        
        assertEquals("rawPassword cannot be empty", exception.getMessage());
    }

    @Test
    void testMatches_NullPassword() {
        // Arrange
        String hashedPassword = passwordService.hashPassword("SomePassword123");

        // Act
        boolean result = passwordService.matches(null, hashedPassword);
        
        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_ValidPassword() {
        // Arrange
        String validPassword = "ValidPassword123";

        // Act
        boolean result = passwordService.isValidPassword(validPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsValidPassword_TooShort() {
        // Arrange
        String shortPassword = "Short1";

        // Act
        boolean result = passwordService.isValidPassword(shortPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_NoUppercase() {
        // Arrange
        String noUppercase = "password123";

        // Act
        boolean result = passwordService.isValidPassword(noUppercase);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_NoLowercase() {
        // Arrange
        String noLowercase = "PASSWORD123";

        // Act
        boolean result = passwordService.isValidPassword(noLowercase);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_NoDigit() {
        // Arrange
        String noDigit = "PasswordABC";

        // Act
        boolean result = passwordService.isValidPassword(noDigit);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_NullPassword() {
        // Act
        boolean result = passwordService.isValidPassword(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_EmptyPassword() {
        // Act
        boolean result = passwordService.isValidPassword("");

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidPassword_ExactMinimumLength() {
        // Arrange
        String minLengthPassword = "Pass1";

        // Act
        boolean result = passwordService.isValidPassword(minLengthPassword);

        // Assert
        assertFalse(result); // Should be 8 characters minimum
    }

    @Test
    void testIsValidPassword_ComplexValidPassword() {
        // Arrange
        String complexPassword = "MySecurePassword123!@#";

        // Act
        boolean result = passwordService.isValidPassword(complexPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void testPasswordHashingPerformance() {
        // Arrange
        String password = "PerformanceTest123";
        long startTime = System.currentTimeMillis();

        // Act
        String hashedPassword = passwordService.hashPassword(password);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        assertNotNull(hashedPassword);
        assertTrue(duration < 1000, "Password hashing should complete within 1 second");
    }

    @Test
    void testMultiplePasswordHashes() {
        // Arrange
        String password = "MultipleHashTest123";
        int iterations = 10;

        // Act & Assert
        for (int i = 0; i < iterations; i++) {
            String hashed = passwordService.hashPassword(password);
            assertNotNull(hashed);
            assertTrue(passwordService.matches(password, hashed));
        }
    }
} 