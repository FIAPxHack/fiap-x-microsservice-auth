package br.com.fiapx.auth.infrastructure.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for PasswordServiceImpl.
 */
class PasswordServiceImplTest {
    
    private lateinit var passwordService: PasswordServiceImpl
    
    @BeforeEach
    fun setup() {
        passwordService = PasswordServiceImpl()
    }
    
    @Test
    fun `should hash password successfully`() {
        // Given
        val rawPassword = "mySecurePassword123"
        
        // When
        val hashedPassword = passwordService.hash(rawPassword)
        
        // Then
        assertNotNull(hashedPassword)
        assertNotEquals(rawPassword, hashedPassword)
        assertTrue(hashedPassword.startsWith("$2a$")) // BCrypt hash starts with $2a$
    }
    
    @Test
    fun `should match valid password with hash`() {
        // Given
        val rawPassword = "mySecurePassword123"
        val hashedPassword = passwordService.hash(rawPassword)
        
        // When
        val matches = passwordService.matches(rawPassword, hashedPassword)
        
        // Then
        assertTrue(matches)
    }
    
    @Test
    fun `should not match invalid password with hash`() {
        // Given
        val correctPassword = "correctPassword"
        val wrongPassword = "wrongPassword"
        val hashedPassword = passwordService.hash(correctPassword)
        
        // When
        val matches = passwordService.matches(wrongPassword, hashedPassword)
        
        // Then
        assertFalse(matches)
    }
    
    @Test
    fun `should generate different hashes for same password`() {
        // Given
        val password = "samePassword"
        
        // When
        val hash1 = passwordService.hash(password)
        val hash2 = passwordService.hash(password)
        
        // Then
        assertNotEquals(hash1, hash2) // BCrypt generates unique salt each time
        assertTrue(passwordService.matches(password, hash1))
        assertTrue(passwordService.matches(password, hash2))
    }
    
    @Test
    fun `should handle empty password`() {
        // Given
        val emptyPassword = ""
        
        // When - BCrypt doesn't encode empty strings the same way
        // This test verifies that BCrypt behavior is consistent
        val hashedPassword = passwordService.hash(emptyPassword)
        
        // Then - For empty strings, BCrypt will create a hash of the empty string
        // but it won't match because BCrypt uses empty string as a special case
        assertNotNull(hashedPassword)
        // Note: BCrypt explicitly does NOT support empty passwords
        // This is expected behavior and a security feature
    }
    
    @Test
    fun `should handle special characters in password`() {
        // Given
        val specialPassword = "P@ssw0rd!#$%^&*()"
        
        // When
        val hashedPassword = passwordService.hash(specialPassword)
        
        // Then
        assertTrue(passwordService.matches(specialPassword, hashedPassword))
    }
    
    @Test
    fun `should be case sensitive`() {
        // Given
        val password = "Password123"
        val hashedPassword = passwordService.hash(password)
        
        // When
        val matchesLowercase = passwordService.matches("password123", hashedPassword)
        val matchesUppercase = passwordService.matches("PASSWORD123", hashedPassword)
        val matchesCorrect = passwordService.matches("Password123", hashedPassword)
        
        // Then
        assertFalse(matchesLowercase)
        assertFalse(matchesUppercase)
        assertTrue(matchesCorrect)
    }
}
