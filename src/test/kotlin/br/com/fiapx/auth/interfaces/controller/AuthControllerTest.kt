package br.com.fiapx.auth.interfaces.controller

import br.com.fiapx.auth.application.usecase.LoginUseCase
import br.com.fiapx.auth.application.usecase.RefreshTokenUseCase
import br.com.fiapx.auth.application.usecase.ValidateTokenUseCase
import br.com.fiapx.auth.interfaces.dto.*
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

/**
 * Unit tests for AuthController.
 */
class AuthControllerTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var validateTokenUseCase: ValidateTokenUseCase
    private lateinit var refreshTokenUseCase: RefreshTokenUseCase
    private lateinit var authController: AuthController

    @BeforeEach
    fun setup() {
        loginUseCase = mockk()
        validateTokenUseCase = mockk()
        refreshTokenUseCase = mockk()
        authController = AuthController(loginUseCase, validateTokenUseCase, refreshTokenUseCase)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `login should return OK with tokens when credentials are valid`() {
        // Given
        val request = LoginRequest(email = "user@example.com", password = "password123")
        val httpRequest = mockk<HttpServletRequest>()
        every { httpRequest.getHeader("X-Forwarded-For") } returns null
        every { httpRequest.remoteAddr } returns "127.0.0.1"

        val output = LoginUseCase.LoginOutput(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            tokenType = "Bearer",
            expiresIn = 900
        )
        every { loginUseCase.execute(any()) } returns output

        // When
        val response = authController.login(request, httpRequest)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("access-token", response.body!!.accessToken)
        assertEquals("refresh-token", response.body!!.refreshToken)
        assertEquals("Bearer", response.body!!.tokenType)
        assertEquals(900, response.body!!.expiresIn)

        verify(exactly = 1) {
            loginUseCase.execute(match {
                it.email == "user@example.com" && it.password == "password123" && it.ipAddress == "127.0.0.1"
            })
        }
    }

    @Test
    fun `login should extract IP from X-Forwarded-For header`() {
        // Given
        val request = LoginRequest(email = "user@example.com", password = "password123")
        val httpRequest = mockk<HttpServletRequest>()
        every { httpRequest.getHeader("X-Forwarded-For") } returns "10.0.0.1, 192.168.1.1"

        val output = LoginUseCase.LoginOutput(
            accessToken = "token",
            refreshToken = "refresh",
            tokenType = "Bearer",
            expiresIn = 900
        )
        every { loginUseCase.execute(any()) } returns output

        // When
        authController.login(request, httpRequest)

        // Then
        verify(exactly = 1) {
            loginUseCase.execute(match { it.ipAddress == "10.0.0.1" })
        }
    }

    @Test
    fun `login should use remoteAddr when X-Forwarded-For is blank`() {
        // Given
        val request = LoginRequest(email = "user@example.com", password = "pass")
        val httpRequest = mockk<HttpServletRequest>()
        every { httpRequest.getHeader("X-Forwarded-For") } returns "   "
        every { httpRequest.remoteAddr } returns "192.168.0.100"

        val output = LoginUseCase.LoginOutput(
            accessToken = "t", refreshToken = "r", tokenType = "Bearer", expiresIn = 900
        )
        every { loginUseCase.execute(any()) } returns output

        // When
        authController.login(request, httpRequest)

        // Then
        verify(exactly = 1) {
            loginUseCase.execute(match { it.ipAddress == "192.168.0.100" })
        }
    }

    @Test
    fun `validateToken should return OK with user info when token is valid`() {
        // Given
        val request = ValidateTokenRequest(token = "valid.jwt.token")
        val userId = UUID.randomUUID()
        val output = ValidateTokenUseCase.ValidateTokenOutput(
            valid = true,
            userId = userId,
            email = "user@example.com",
            role = "USER"
        )
        every { validateTokenUseCase.execute(any()) } returns output

        // When
        val response = authController.validateToken(request)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.valid)
        assertEquals(userId.toString(), response.body!!.userId)
        assertEquals("user@example.com", response.body!!.email)
        assertEquals("USER", response.body!!.role)

        verify(exactly = 1) {
            validateTokenUseCase.execute(match { it.token == "valid.jwt.token" })
        }
    }

    @Test
    fun `refreshToken should return OK with new tokens`() {
        // Given
        val request = RefreshTokenRequest(refreshToken = "old-refresh-token")
        val output = RefreshTokenUseCase.RefreshTokenOutput(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            tokenType = "Bearer"
        )
        every { refreshTokenUseCase.execute(any()) } returns output

        // When
        val response = authController.refreshToken(request)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("new-access-token", response.body!!.accessToken)
        assertEquals("new-refresh-token", response.body!!.refreshToken)
        assertEquals("Bearer", response.body!!.tokenType)

        verify(exactly = 1) {
            refreshTokenUseCase.execute(match { it.refreshToken == "old-refresh-token" })
        }
    }

    @Test
    fun `login should extract single IP from X-Forwarded-For header`() {
        // Given
        val request = LoginRequest(email = "user@example.com", password = "pass")
        val httpRequest = mockk<HttpServletRequest>()
        every { httpRequest.getHeader("X-Forwarded-For") } returns "203.0.113.50"

        val output = LoginUseCase.LoginOutput(
            accessToken = "t", refreshToken = "r", tokenType = "Bearer", expiresIn = 900
        )
        every { loginUseCase.execute(any()) } returns output

        // When
        authController.login(request, httpRequest)

        // Then
        verify(exactly = 1) {
            loginUseCase.execute(match { it.ipAddress == "203.0.113.50" })
        }
    }
}
