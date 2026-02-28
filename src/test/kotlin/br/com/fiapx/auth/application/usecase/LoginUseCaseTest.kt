package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.exception.InvalidCredentialsException
import br.com.fiapx.auth.domain.exception.UserNotFoundException
import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.repository.LoginAttemptRepository
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.PasswordService
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
 * Unit tests for LoginUseCase.
 */
class LoginUseCaseTest {
    
    private lateinit var userService: UserService
    private lateinit var passwordService: PasswordService
    private lateinit var jwtService: JwtService
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var loginAttemptRepository: LoginAttemptRepository
    private lateinit var loginUseCase: LoginUseCase
    
    private val accessTokenExpirationMs = 900000L // 15 minutes
    private val refreshTokenExpirationMs = 604800000L // 7 days
    
    @BeforeEach
    fun setup() {
        userService = mockk()
        passwordService = mockk()
        jwtService = mockk()
        refreshTokenRepository = mockk()
        loginAttemptRepository = mockk()
        
        loginUseCase = LoginUseCase(
            userService = userService,
            passwordService = passwordService,
            jwtService = jwtService,
            refreshTokenRepository = refreshTokenRepository,
            loginAttemptRepository = loginAttemptRepository,
            accessTokenExpirationMs = accessTokenExpirationMs,
            refreshTokenExpirationMs = refreshTokenExpirationMs
        )
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `should login successfully with valid credentials`() {
        // Given
        val email = "user@example.com"
        val password = "password123"
        val ipAddress = "192.168.1.1"
        
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = email,
            passwordHash = "hashedPassword",
            role = "USER"
        )
        
        val accessToken = "access.token.here"
        val refreshTokenValue = "refresh-token-uuid"
        
        every { userService.findByEmail(email) } returns user
        every { passwordService.matches(password, user.passwordHash) } returns true
        every { jwtService.generateAccessToken(userId, email, "USER") } returns accessToken
        every { jwtService.generateRefreshToken() } returns refreshTokenValue
        every { loginAttemptRepository.save(any()) } returns mockk()
        every { refreshTokenRepository.save(any()) } returns mockk()
        
        // When
        val input = LoginUseCase.LoginInput(email, password, ipAddress)
        val output = loginUseCase.execute(input)
        
        // Then
        assertNotNull(output)
        assertEquals(accessToken, output.accessToken)
        assertEquals(refreshTokenValue, output.refreshToken)
        assertEquals("Bearer", output.tokenType)
        
        verify(exactly = 1) { userService.findByEmail(email) }
        verify(exactly = 1) { passwordService.matches(password, user.passwordHash) }
        verify(exactly = 1) { jwtService.generateAccessToken(userId, email, "USER") }
        verify(exactly = 1) { jwtService.generateRefreshToken() }
        verify(exactly = 1) { 
            loginAttemptRepository.save(match { 
                it.email == email && it.success && it.ipAddress == ipAddress 
            }) 
        }
        verify(exactly = 1) { 
            refreshTokenRepository.save(match { 
                it.userId == userId && it.token == refreshTokenValue && !it.revoked 
            }) 
        }
    }
    
    @Test
    fun `should throw InvalidCredentialsException when password is incorrect`() {
        // Given
        val email = "user@example.com"
        val password = "wrongPassword"
        val ipAddress = "192.168.1.1"
        
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = "hashedPassword",
            role = "USER"
        )
        
        every { userService.findByEmail(email) } returns user
        every { passwordService.matches(password, user.passwordHash) } returns false
        every { loginAttemptRepository.save(any()) } returns mockk()
        
        // When & Then
        val input = LoginUseCase.LoginInput(email, password, ipAddress)
        assertThrows<InvalidCredentialsException> {
            loginUseCase.execute(input)
        }
        
        verify(exactly = 1) { userService.findByEmail(email) }
        verify(exactly = 1) { passwordService.matches(password, user.passwordHash) }
        verify(exactly = 1) { 
            loginAttemptRepository.save(match { 
                it.email == email && !it.success && it.ipAddress == ipAddress 
            }) 
        }
        verify(exactly = 0) { jwtService.generateAccessToken(any(), any(), any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }
    
    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Given
        val email = "nonexistent@example.com"
        val password = "password123"
        val ipAddress = "192.168.1.1"
        
        every { userService.findByEmail(email) } throws UserNotFoundException("User not found")
        
        // When & Then
        val input = LoginUseCase.LoginInput(email, password, ipAddress)
        assertThrows<UserNotFoundException> {
            loginUseCase.execute(input)
        }
        
        verify(exactly = 1) { userService.findByEmail(email) }
        verify(exactly = 0) { passwordService.matches(any(), any()) }
        verify(exactly = 0) { loginAttemptRepository.save(any()) }
    }
    
    @Test
    fun `should record login attempt even when password is wrong`() {
        // Given
        val email = "user@example.com"
        val password = "wrongPassword"
        val ipAddress = "192.168.1.1"
        
        val user = User(
            id = UUID.randomUUID(),
            email = email,
            passwordHash = "hashedPassword",
            role = "USER"
        )
        
        val capturedAttempts = mutableListOf<LoginAttempt>()
        
        every { userService.findByEmail(email) } returns user
        every { passwordService.matches(password, user.passwordHash) } returns false
        every { loginAttemptRepository.save(capture(capturedAttempts)) } returns mockk()
        
        // When
        val input = LoginUseCase.LoginInput(email, password, ipAddress)
        try {
            loginUseCase.execute(input)
        } catch (e: InvalidCredentialsException) {
            // Expected
        }
        
        // Then
        assertEquals(1, capturedAttempts.size)
        val attempt = capturedAttempts[0]
        assertEquals(email, attempt.email)
        assertEquals(false, attempt.success)
        assertEquals(ipAddress, attempt.ipAddress)
    }
    
    @Test
    fun `should generate refresh token with correct expiration`() {
        // Given
        val email = "user@example.com"
        val password = "password123"
        val userId = UUID.randomUUID()
        
        val user = User(id = userId, email = email, passwordHash = "hash", role = "USER")
        val capturedTokens = mutableListOf<RefreshToken>()
        
        every { userService.findByEmail(email) } returns user
        every { passwordService.matches(password, user.passwordHash) } returns true
        every { jwtService.generateAccessToken(any(), any(), any()) } returns "token"
        every { jwtService.generateRefreshToken() } returns "refresh"
        every { loginAttemptRepository.save(any()) } returns mockk()
        every { refreshTokenRepository.save(capture(capturedTokens)) } returns mockk()
        
        // When
        val input = LoginUseCase.LoginInput(email, password, null)
        loginUseCase.execute(input)
        
        // Then
        assertEquals(1, capturedTokens.size)
        val token = capturedTokens[0]
        assertTrue(token.expiresAt.isAfter(Instant.now()))
        assertTrue(token.expiresAt.isBefore(Instant.now().plusMillis(refreshTokenExpirationMs + 1000)))
    }
}
