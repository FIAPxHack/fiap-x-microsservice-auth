package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.config.AuthMetrics
import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import br.com.fiapx.auth.domain.exception.TokenRevokedException
import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.UserService
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory

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
    private val enableTokenRotation: Boolean = true,
    private val authMetrics: AuthMetrics? = null
) {
    private val logger = LoggerFactory.getLogger(RefreshTokenUseCase::class.java)
    
    fun execute(input: RefreshTokenInput): RefreshTokenOutput {
        return authMetrics?.refreshTimer?.recordCallable<RefreshTokenOutput> {
            executeInternal(input)
        } ?: executeInternal(input)
    }

    private fun executeInternal(input: RefreshTokenInput): RefreshTokenOutput {
        logger.debug("[REFRESH_TOKEN_USE_CASE] iniciando execução - tamanho do refresh token={}", input.refreshToken.length)

        // Find refresh token
        val refreshToken = refreshTokenRepository.findByToken(input.refreshToken)
            ?: run {
                logger.debug("[REFRESH_TOKEN_USE_CASE] refresh token não encontrado")
                authMetrics?.refreshFailureCounter?.increment()
                throw InvalidTokenException("Refresh token not found")
            }

        logger.debug("[REFRESH_TOKEN_USE_CASE] refresh token encontrado id={} userId={}", refreshToken.id, refreshToken.userId)

        // Check if token is revoked
        if (refreshToken.revoked) {
            logger.debug("[REFRESH_TOKEN_USE_CASE] refresh token revogado id={}", refreshToken.id)
            authMetrics?.refreshFailureCounter?.increment()
            throw TokenRevokedException("Refresh token has been revoked")
        }
        
        // Check if token has expired
        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            logger.debug("[REFRESH_TOKEN_USE_CASE] refresh token expirado id={} expiresAt={}", refreshToken.id, refreshToken.expiresAt)
            authMetrics?.refreshFailureCounter?.increment()
            throw TokenExpiredException("Refresh token has expired")
        }
        
        // Get user information
        val user = userService.findById(refreshToken.userId)
        logger.debug("[REFRESH_TOKEN_USE_CASE] usuário encontrado id={}", user.id)
        
        // Generate new access token
        val newAccessToken = jwtService.generateAccessToken(user.id, user.email, user.role)
        logger.debug("[REFRESH_TOKEN_USE_CASE] novo access token gerado para userId={}", user.id)
        
        // Handle token rotation if enabled
        val newRefreshToken = if (enableTokenRotation) {
            // Revoke old refresh token
            refreshTokenRepository.deleteById(refreshToken.id)
            logger.debug("[REFRESH_TOKEN_USE_CASE] refresh token antigo revogado id={}", refreshToken.id)
            
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
            logger.debug("[REFRESH_TOKEN_USE_CASE] novo refresh token salvo id={} userId={}", newToken.id, newToken.userId)
            newRefreshTokenValue
        } else {
            logger.debug("[REFRESH_TOKEN_USE_CASE] rotação de token desabilitada, mantendo o mesmo refresh token id={}", refreshToken.id)
            // Keep the same refresh token
            input.refreshToken
        }
        
        logger.debug("[REFRESH_TOKEN_USE_CASE] refresh realizado com sucesso para userId={}", refreshToken.userId)

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
