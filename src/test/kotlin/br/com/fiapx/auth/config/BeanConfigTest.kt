package br.com.fiapx.auth.config

import br.com.fiapx.auth.application.usecase.LoginUseCase
import br.com.fiapx.auth.application.usecase.RefreshTokenUseCase
import br.com.fiapx.auth.application.usecase.ValidateTokenUseCase
import br.com.fiapx.auth.domain.repository.LoginAttemptRepository
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.PasswordService
import br.com.fiapx.auth.domain.service.UserService
import br.com.fiapx.auth.infrastructure.jwt.JwtServiceImpl
import br.com.fiapx.auth.infrastructure.security.PasswordServiceImpl
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

/**
 * Unit tests for BeanConfig.
 */
class BeanConfigTest {

    private lateinit var beanConfig: BeanConfig

    @BeforeEach
    fun setup() {
        beanConfig = BeanConfig()
        ReflectionTestUtils.setField(beanConfig, "jwtSecret", "test-secret-key-must-be-at-least-256-bits-long-for-hs256")
        ReflectionTestUtils.setField(beanConfig, "accessTokenExpiration", 900000L)
        ReflectionTestUtils.setField(beanConfig, "refreshTokenExpiration", 604800000L)
        ReflectionTestUtils.setField(beanConfig, "jwtIssuer", "test-issuer")
        ReflectionTestUtils.setField(beanConfig, "userServiceBaseUrl", "http://localhost:8081")
        ReflectionTestUtils.setField(beanConfig, "userServiceTimeout", 5000L)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `jwtService should return JwtServiceImpl instance`() {
        val jwtService = beanConfig.jwtService()
        assertNotNull(jwtService)
        assertTrue(jwtService is JwtServiceImpl)
    }

    @Test
    fun `passwordService should return PasswordServiceImpl instance`() {
        val passwordService = beanConfig.passwordService()
        assertNotNull(passwordService)
        assertTrue(passwordService is PasswordServiceImpl)
    }

    @Test
    fun `webClient should be created with base URL`() {
        val webClient = beanConfig.webClient()
        assertNotNull(webClient)
    }

    @Test
    fun `loginUseCase should be created with all dependencies`() {
        val userService = mockk<UserService>()
        val passwordService = mockk<PasswordService>()
        val jwtService = mockk<JwtService>()
        val refreshTokenRepository = mockk<RefreshTokenRepository>()
        val loginAttemptRepository = mockk<LoginAttemptRepository>()

        val loginUseCase = beanConfig.loginUseCase(
            userService, passwordService, jwtService, refreshTokenRepository, loginAttemptRepository
        )
        assertNotNull(loginUseCase)
        assertTrue(loginUseCase is LoginUseCase)
    }

    @Test
    fun `validateTokenUseCase should be created with jwtService`() {
        val jwtService = mockk<JwtService>()
        val validateTokenUseCase = beanConfig.validateTokenUseCase(jwtService)
        assertNotNull(validateTokenUseCase)
        assertTrue(validateTokenUseCase is ValidateTokenUseCase)
    }

    @Test
    fun `refreshTokenUseCase should be created with all dependencies`() {
        val refreshTokenRepository = mockk<RefreshTokenRepository>()
        val userService = mockk<UserService>()
        val jwtService = mockk<JwtService>()

        val refreshTokenUseCase = beanConfig.refreshTokenUseCase(
            refreshTokenRepository, userService, jwtService
        )
        assertNotNull(refreshTokenUseCase)
        assertTrue(refreshTokenUseCase is RefreshTokenUseCase)
    }
}
