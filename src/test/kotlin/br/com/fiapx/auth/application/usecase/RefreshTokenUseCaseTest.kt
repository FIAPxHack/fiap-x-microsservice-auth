package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import br.com.fiapx.auth.domain.exception.TokenRevokedException
import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.UserService
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for RefreshTokenUseCase.
 */
class RefreshTokenUseCaseTest {
    
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var userService: UserService
    private lateinit var jwtService: JwtService
    private lateinit var refreshTokenUseCase: RefreshTokenUseCase
    
    private val refreshTokenExpirationMs = 604800000L // 7 days
    
    @BeforeEach
    fun setup() {
        refreshTokenRepository = mockk()
        userService = mockk()
        jwtService = mockk()
        
        refreshTokenUseCase = RefreshTokenUseCase(
            refreshTokenRepository = refreshTokenRepository,
            userService = userService,
            jwtService = jwtService,
            refreshTokenExpirationMs = refreshTokenExpirationMs,
            enableTokenRotation = true
        )
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should refresh token successfully with valid refresh token`() {
        // Given
        val refreshTokenValue = "valid-refresh-token"
        val userId = UUID.randomUUID()
        val email = "user@example.com"
        val role = "USER"
        
        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            userId = userId,
            token = refreshTokenValue,
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = false,
            createdAt = Instant.now().minusSeconds(300)
        )
        
        val user = User(
            id = userId,
            email = email,
            passwordHash = "hash",
            role = role
        )
        
        val newAccessToken = "new.access.token"
        val newRefreshTokenValue = "new-refresh-token"
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns refreshToken
        every { userService.findById(userId) } returns user
        every { jwtService.generateAccessToken(userId, email, role) } returns newAccessToken
        every { jwtService.generateRefreshToken() } returns newRefreshTokenValue
        every { refreshTokenRepository.deleteById(refreshToken.id) } just Runs
        every { refreshTokenRepository.save(any()) } returns mockk()
        
        // When
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        val output = refreshTokenUseCase.execute(input)
        
        // Then
        assertEquals(newAccessToken, output.accessToken)
        assertEquals(newRefreshTokenValue, output.refreshToken)
        assertEquals("Bearer", output.tokenType)
        
        verify(exactly = 1) { refreshTokenRepository.findByToken(refreshTokenValue) }
        verify(exactly = 1) { userService.findById(userId) }
        verify(exactly = 1) { jwtService.generateAccessToken(userId, email, role) }
        verify(exactly = 1) { refreshTokenRepository.deleteById(refreshToken.id) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
    }
    
    @Test
    fun `should throw InvalidTokenException when refresh token not found`() {
        // Given
        val refreshTokenValue = "non-existent-token"
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns null
        
        // When & Then
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        assertThrows<InvalidTokenException> {
            refreshTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { refreshTokenRepository.findByToken(refreshTokenValue) }
        verify(exactly = 0) { userService.findById(any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any(), any(), any()) }
    }
    
    @Test
    fun `should throw TokenRevokedException when refresh token is revoked`() {
        // Given
        val refreshTokenValue = "revoked-token"
        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            token = refreshTokenValue,
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = true, // Token is revoked
            createdAt = Instant.now().minusSeconds(300)
        )
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns refreshToken
        
        // When & Then
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        assertThrows<TokenRevokedException> {
            refreshTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { refreshTokenRepository.findByToken(refreshTokenValue) }
        verify(exactly = 0) { userService.findById(any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any(), any(), any()) }
    }
    
    @Test
    fun `should throw TokenExpiredException when refresh token is expired`() {
        // Given
        val refreshTokenValue = "expired-token"
        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            token = refreshTokenValue,
            expiresAt = Instant.now().minusSeconds(3600), // Expired
            revoked = false,
            createdAt = Instant.now().minusSeconds(7200)
        )
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns refreshToken
        
        // When & Then
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        assertThrows<TokenExpiredException> {
            refreshTokenUseCase.execute(input)
        }
        
        verify(exactly = 1) { refreshTokenRepository.findByToken(refreshTokenValue) }
        verify(exactly = 0) { userService.findById(any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any(), any(), any()) }
    }
    
    @Test
    fun `should rotate refresh token when rotation is enabled`() {
        // Given
        val refreshTokenValue = "old-refresh-token"
        val userId = UUID.randomUUID()
        val tokenId = UUID.randomUUID()
        
        val refreshToken = RefreshToken(
            id = tokenId,
            userId = userId,
            token = refreshTokenValue,
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = false,
            createdAt = Instant.now().minusSeconds(300)
        )
        
        val user = User(
            id = userId,
            email = "user@example.com",
            passwordHash = "hash",
            role = "USER"
        )
        
        val capturedTokens = mutableListOf<RefreshToken>()
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns refreshToken
        every { userService.findById(userId) } returns user
        every { jwtService.generateAccessToken(any(), any(), any()) } returns "new.token"
        every { jwtService.generateRefreshToken() } returns "new-refresh-token"
        every { refreshTokenRepository.deleteById(tokenId) } just Runs
        every { refreshTokenRepository.save(capture(capturedTokens)) } returns mockk()
        
        // When
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        val output = refreshTokenUseCase.execute(input)
        
        // Then
        assertNotEquals(refreshTokenValue, output.refreshToken)
        verify(exactly = 1) { refreshTokenRepository.deleteById(tokenId) }
        assertEquals(1, capturedTokens.size)
        assertFalse(capturedTokens[0].revoked)
    }
    
    @Test
    fun `should not rotate refresh token when rotation is disabled`() {
        // Given
        val refreshTokenValue = "static-refresh-token"
        val userId = UUID.randomUUID()
        
        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            userId = userId,
            token = refreshTokenValue,
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = false,
            createdAt = Instant.now().minusSeconds(300)
        )
        
        val user = User(
            id = userId,
            email = "user@example.com",
            passwordHash = "hash",
            role = "USER"
        )
        
        val useCaseWithoutRotation = RefreshTokenUseCase(
            refreshTokenRepository = refreshTokenRepository,
            userService = userService,
            jwtService = jwtService,
            refreshTokenExpirationMs = refreshTokenExpirationMs,
            enableTokenRotation = false // Rotation disabled
        )
        
        every { refreshTokenRepository.findByToken(refreshTokenValue) } returns refreshToken
        every { userService.findById(userId) } returns user
        every { jwtService.generateAccessToken(any(), any(), any()) } returns "new.token"
        
        // When
        val input = RefreshTokenUseCase.RefreshTokenInput(refreshTokenValue)
        val output = useCaseWithoutRotation.execute(input)
        
        // Then
        assertEquals(refreshTokenValue, output.refreshToken) // Same token returned
        verify(exactly = 0) { refreshTokenRepository.deleteById(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }
}
