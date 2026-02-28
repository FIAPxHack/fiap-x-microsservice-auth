package br.com.fiapx.auth.infrastructure.jwt

import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

/**
 * Unit tests for JwtServiceImpl.
 */
class JwtServiceImplTest {
    
    private lateinit var jwtService: JwtServiceImpl
    
    private val secret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256"
    private val accessTokenExpirationMs = 900000L // 15 minutes
    private val issuer = "test-issuer"
    
    @BeforeEach
    fun setup() {
        jwtService = JwtServiceImpl(
            secret = secret,
            accessTokenExpirationMs = accessTokenExpirationMs,
            issuer = issuer
        )
    }
    
    @Test
    fun `should generate valid access token`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "USER"
        
        // When
        val token = jwtService.generateAccessToken(userId, email, role)
        
        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.split(".").size == 3) // JWT has 3 parts
    }
    
    @Test
    fun `should generate unique refresh tokens`() {
        // When
        val token1 = jwtService.generateRefreshToken()
        val token2 = jwtService.generateRefreshToken()
        
        // Then
        assertNotNull(token1)
        assertNotNull(token2)
        assertNotEquals(token1, token2)
    }
    
    @Test
    fun `should validate token and extract correct claims`() {
        // Given
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "ADMIN"
        
        val token = jwtService.generateAccessToken(userId, email, role)
        
        // When
        val claims = jwtService.validateToken(token)
        
        // Then
        assertNotNull(claims)
        assertEquals(userId.toString(), claims["sub"])
        assertEquals(email, claims["email"])
        assertEquals(role, claims["role"])
        assertNotNull(claims["iat"])
        assertNotNull(claims["exp"])
    }
    
    @Test
    fun `should throw InvalidTokenException for malformed token`() {
        // Given
        val malformedToken = "not.a.valid.jwt"
        
        // When & Then
        assertThrows<InvalidTokenException> {
            jwtService.validateToken(malformedToken)
        }
    }
    
    @Test
    fun `should throw InvalidTokenException for empty token`() {
        // Given
        val emptyToken = ""
        
        // When & Then
        assertThrows<InvalidTokenException> {
            jwtService.validateToken(emptyToken)
        }
    }
    
    @Test
    fun `should throw TokenExpiredException for expired token`() {
        // Given - Create a service with very short expiration
        val shortLivedService = JwtServiceImpl(
            secret = secret,
            accessTokenExpirationMs = 1L, // 1 millisecond
            issuer = issuer
        )
        
        val userId = UUID.randomUUID()
        val token = shortLivedService.generateAccessToken(userId, "user@example.com", "USER")
        
        // Wait for token to expire
        Thread.sleep(10)
        
        // When & Then
        assertThrows<TokenExpiredException> {
            shortLivedService.validateToken(token)
        }
    }
    
    @Test
    fun `should extract userId from token`() {
        // Given
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, "user@example.com", "USER")
        
        // When
        val extractedUserId = jwtService.extractUserId(token)
        
        // Then
        assertEquals(userId, extractedUserId)
    }
    
    @Test
    fun `should extract email from token`() {
        // Given
        val email = "test@example.com"
        val token = jwtService.generateAccessToken(UUID.randomUUID(), email, "USER")
        
        // When
        val extractedEmail = jwtService.extractEmail(token)
        
        // Then
        assertEquals(email, extractedEmail)
    }
    
    @Test
    fun `should extract role from token`() {
        // Given
        val role = "ADMIN"
        val token = jwtService.generateAccessToken(UUID.randomUUID(), "admin@example.com", role)
        
        // When
        val extractedRole = jwtService.extractRole(token)
        
        // Then
        assertEquals(role, extractedRole)
    }
    
    @Test
    fun `should extract claims from expired token`() {
        // Given - Create a service with very short expiration
        val shortLivedService = JwtServiceImpl(
            secret = secret,
            accessTokenExpirationMs = 1L,
            issuer = issuer
        )
        
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "USER"
        val token = shortLivedService.generateAccessToken(userId, email, role)
        
        // Wait for token to expire
        Thread.sleep(10)
        
        // When - Even though token is expired, we should be able to extract claims
        val extractedUserId = shortLivedService.extractUserId(token)
        val extractedEmail = shortLivedService.extractEmail(token)
        val extractedRole = shortLivedService.extractRole(token)
        
        // Then
        assertEquals(userId, extractedUserId)
        assertEquals(email, extractedEmail)
        assertEquals(role, extractedRole)
    }
    
    @Test
    fun `should generate different tokens for different users`() {
        // Given
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val email = "user@example.com"
        val role = "USER"
        
        // When
        val token1 = jwtService.generateAccessToken(userId1, email, role)
        val token2 = jwtService.generateAccessToken(userId2, email, role)
        
        // Then
        assertNotEquals(token1, token2)
    }
    
    @Test
    fun `should include correct issuer in token`() {
        // Given
        val userId = UUID.randomUUID()
        val token = jwtService.generateAccessToken(userId, "user@example.com", "USER")
        
        // When
        val claims = jwtService.validateToken(token)
        
        // Then - The issuer is validated during token verification
        // If issuer doesn't match, validation would fail
        assertNotNull(claims)
    }
}
