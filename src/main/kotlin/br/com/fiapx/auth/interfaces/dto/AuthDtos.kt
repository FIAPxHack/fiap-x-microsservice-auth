package br.com.fiapx.auth.interfaces.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request DTO for login endpoint.
 */
data class LoginRequest(
    @field:Email(message = "[VALIDAÇÃO] O e-mail deve ser válido")
    @field:NotBlank(message = "[VALIDAÇÃO] O e-mail é obrigatório")
    val email: String,
    
    @field:NotBlank(message = "[VALIDAÇÃO] A senha é necessária")
    val password: String
)

/**
 * Response DTO for login endpoint.
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)

/**
 * Request DTO for token validation endpoint.
 */
data class ValidateTokenRequest(
    @field:NotBlank(message = "[VALIDAÇÃO] Token é necessário")
    val token: String
)

/**
 * Response DTO for token validation endpoint.
 */
data class ValidateTokenResponse(
    val valid: Boolean,
    val userId: String,
    val email: String,
    val role: String
)

/**
 * Request DTO for refresh token endpoint.
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "[VALIDAÇÃO] Token de atualização é necessário")
    val refreshToken: String
)

/**
 * Response DTO for refresh token endpoint.
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)

/**
 * Generic error response DTO.
 */
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
