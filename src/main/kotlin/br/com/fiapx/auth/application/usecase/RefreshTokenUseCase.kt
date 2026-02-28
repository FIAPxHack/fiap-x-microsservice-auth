package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import br.com.fiapx.auth.domain.exception.TokenRevokedException
import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.UserService
import java.time.Instant
import java.util.UUID

/**
 * Caso de uso para atualizar tokens de acesso.
 * Valida o token de atualização e gera um novo token de acesso.
 * Opcionalmente gira o token de atualização.
 */
class RefreshTokenUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val refreshTokenExpirationMs: Long,
    private val enableTokenRotation: Boolean = true
) {
    
    fun execute(input: RefreshTokenInput): RefreshTokenOutput {
        // Find refresh token
        val refreshToken = refreshTokenRepository.findByToken(input.refreshToken)
            ?: throw InvalidTokenException("Refresh token not found")
        
        // Check if token is revoked
        if (refreshToken.revoked) {
            throw TokenRevokedException("Refresh token has been revoked")
        }
        
        // Check if token has expired
        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            throw TokenExpiredException("Refresh token has expired")
        }
        
        // Get user information
        val user = userService.findById(refreshToken.userId)
        
        // Generate new access token
        val newAccessToken = jwtService.generateAccessToken(user.id, user.email, user.role)
        
        // Handle token rotation if enabled
        val newRefreshToken = if (enableTokenRotation) {
            // Revoke old refresh token
            refreshTokenRepository.deleteById(refreshToken.id)
            
            // Generate new refresh token
            val newRefreshTokenValue = jwtService.generateRefreshToken()
            val newToken = RefreshToken(
                id = UUID.randomUUID(),
                userId = user.id,
                token = newRefreshTokenValue,
                expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs),
                revoked = false,
                createdAt = Instant.now()
            )
            refreshTokenRepository.save(newToken)
            newRefreshTokenValue
        } else {
            // Keep the same refresh token
            input.refreshToken
        }
        
        return RefreshTokenOutput(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            tokenType = "Bearer"
        )
    }
    
    data class RefreshTokenInput(
        val refreshToken: String
    )
    
    data class RefreshTokenOutput(
        val accessToken: String,
        val refreshToken: String,
        val tokenType: String
    )
}
