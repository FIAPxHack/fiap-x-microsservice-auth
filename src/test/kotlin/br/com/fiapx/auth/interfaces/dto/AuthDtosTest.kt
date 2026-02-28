package br.com.fiapx.auth.interfaces.dto

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for Auth DTOs.
 */
class AuthDtosTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    // --- LoginRequest tests ---

    @Test
    fun `LoginRequest should be valid with correct email and password`() {
        val request = LoginRequest(email = "user@example.com", password = "password123")
        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `LoginRequest should fail validation with invalid email`() {
        val request = LoginRequest(email = "invalid-email", password = "password123")
        val violations = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
    }

    @Test
    fun `LoginRequest should fail validation with blank email`() {
        val request = LoginRequest(email = "", password = "password123")
        val violations = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
    }

    @Test
    fun `LoginRequest should fail validation with blank password`() {
        val request = LoginRequest(email = "user@example.com", password = "")
        val violations = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "password" })
    }

    @Test
    fun `LoginRequest data class should have correct properties`() {
        val request = LoginRequest(email = "test@mail.com", password = "pass")
        assertEquals("test@mail.com", request.email)
        assertEquals("pass", request.password)
    }

    @Test
    fun `LoginRequest data class copy should work`() {
        val request = LoginRequest(email = "a@b.com", password = "p")
        val copy = request.copy(password = "newpass")
        assertEquals("a@b.com", copy.email)
        assertEquals("newpass", copy.password)
    }

    // --- LoginResponse tests ---

    @Test
    fun `LoginResponse should store all fields correctly`() {
        val response = LoginResponse(
            accessToken = "access",
            refreshToken = "refresh",
            tokenType = "Bearer",
            expiresIn = 900
        )
        assertEquals("access", response.accessToken)
        assertEquals("refresh", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(900, response.expiresIn)
    }

    @Test
    fun `LoginResponse equals and hashCode should work correctly`() {
        val r1 = LoginResponse("a", "b", "Bearer", 100)
        val r2 = LoginResponse("a", "b", "Bearer", 100)
        assertEquals(r1, r2)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    // --- ValidateTokenRequest tests ---

    @Test
    fun `ValidateTokenRequest should be valid with non-blank token`() {
        val request = ValidateTokenRequest(token = "some.jwt.token")
        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `ValidateTokenRequest should fail validation with blank token`() {
        val request = ValidateTokenRequest(token = "")
        val violations = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "token" })
    }

    // --- ValidateTokenResponse tests ---

    @Test
    fun `ValidateTokenResponse should store all fields correctly`() {
        val response = ValidateTokenResponse(
            valid = true,
            userId = "user-id-123",
            email = "user@example.com",
            role = "ADMIN"
        )
        assertTrue(response.valid)
        assertEquals("user-id-123", response.userId)
        assertEquals("user@example.com", response.email)
        assertEquals("ADMIN", response.role)
    }

    // --- RefreshTokenRequest tests ---

    @Test
    fun `RefreshTokenRequest should be valid with non-blank refreshToken`() {
        val request = RefreshTokenRequest(refreshToken = "valid-refresh-token")
        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `RefreshTokenRequest should fail validation with blank refreshToken`() {
        val request = RefreshTokenRequest(refreshToken = "")
        val violations = validator.validate(request)
        assertTrue(violations.isNotEmpty())
        assertTrue(violations.any { it.propertyPath.toString() == "refreshToken" })
    }

    // --- RefreshTokenResponse tests ---

    @Test
    fun `RefreshTokenResponse should store all fields correctly`() {
        val response = RefreshTokenResponse(
            accessToken = "new-access",
            refreshToken = "new-refresh",
            tokenType = "Bearer"
        )
        assertEquals("new-access", response.accessToken)
        assertEquals("new-refresh", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
    }

    // --- ErrorResponse tests ---

    @Test
    fun `ErrorResponse should store all fields correctly`() {
        val error = ErrorResponse(
            timestamp = "2024-01-01T00:00:00Z",
            status = 400,
            error = "Bad Request",
            message = "Validation failed",
            path = "/auth/login"
        )
        assertEquals("2024-01-01T00:00:00Z", error.timestamp)
        assertEquals(400, error.status)
        assertEquals("Bad Request", error.error)
        assertEquals("Validation failed", error.message)
        assertEquals("/auth/login", error.path)
    }

    @Test
    fun `ErrorResponse equals and hashCode should work correctly`() {
        val e1 = ErrorResponse("ts", 500, "err", "msg", "/path")
        val e2 = ErrorResponse("ts", 500, "err", "msg", "/path")
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
    }

    @Test
    fun `ErrorResponse toString should contain all fields`() {
        val error = ErrorResponse("ts", 404, "Not Found", "msg", "/test")
        val str = error.toString()
        assertTrue(str.contains("404"))
        assertTrue(str.contains("Not Found"))
        assertTrue(str.contains("/test"))
    }
}
