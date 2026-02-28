package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import br.com.fiapx.auth.domain.service.JwtService
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for ValidateTokenUseCase.
 */
class ValidateTokenUseCaseTest {
    
    private lateinit var jwtService: JwtService
    private lateinit var validateTokenUseCase: ValidateTokenUseCase
    
    @BeforeEach
    fun setup() {
        jwtService = mockk()
        validateTokenUseCase = ValidateTokenUseCase(jwtService)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should validate token successfully`() {
        // Given
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "USER"
        
        val claims = mapOf(
            "sub" to userId.toString(),
            "email" to email,
            "role" to role,
            "iat" to Instant.now(),
            "exp" to Instant.now().plusSeconds(900)
        )
        
        every { jwtService.validateToken(token) } returns claims
        
        // When
        val input = ValidateTokenUseCase.ValidateTokenInput(token)
        val output = validateTokenUseCase.execute(input)
        
        // Then
        assertTrue(output.valid)
        assertEquals(userId, output.userId)
        assertEquals(email, output.email)
        assertEquals(role, output.role)
        
        verify(exactly = 1) { jwtService.validateToken(token) }
    }
    
    @Test
    fun `should throw TokenExpiredException when token is expired`() {
        // Given
        val token = "expired.jwt.token"
        
        every { jwtService.validateToken(token) } throws TokenExpiredException("Token has expired")
        
        // When & Then
        val input = ValidateTokenUseCase.ValidateTokenInput(token)
        assertThrows<TokenExpiredException> {
            validateTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { jwtService.validateToken(token) }
    }
    
    @Test
    fun `should throw InvalidTokenException when token is invalid`() {
        // Given
        val token = "invalid.jwt.token"
        
        every { jwtService.validateToken(token) } throws InvalidTokenException("Invalid token")
        
        // When & Then
        val input = ValidateTokenUseCase.ValidateTokenInput(token)
        assertThrows<InvalidTokenException> {
            validateTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { jwtService.validateToken(token) }
    }
    
    @Test
    fun `should handle malformed token`() {
        // Given
        val token = "malformed-token"
        
        every { jwtService.validateToken(token) } throws InvalidTokenException("Malformed token")
        
        // When & Then
        val input = ValidateTokenUseCase.ValidateTokenInput(token)
        assertThrows<InvalidTokenException> {
            validateTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { jwtService.validateToken(token) }
    }
    
    @Test
    fun `should correctly extract all user information from token`() {
        // Given
        val token = "valid.jwt.token"
        val userId = UUID.randomUUID()
        val email = "admin@example.com"
        val role = "ADMIN"
        
        val claims = mapOf(
            "sub" to userId.toString(),
            "email" to email,
            "role" to role,
            "iat" to Instant.now().minusSeconds(300),
            "exp" to Instant.now().plusSeconds(600)
        )
        
        every { jwtService.validateToken(token) } returns claims
        
        // When
        val input = ValidateTokenUseCase.ValidateTokenInput(token)
        val output = validateTokenUseCase.execute(input)
        
        // Then
        assertEquals(userId, output.userId)
        assertEquals(email, output.email)
        assertEquals(role, output.role)
        assertTrue(output.valid)
    }
}
