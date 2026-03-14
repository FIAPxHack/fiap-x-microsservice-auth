package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.config.AuthMetrics
import br.com.fiapx.auth.domain.service.JwtService
import java.util.UUID
import org.slf4j.LoggerFactory

/**
 * Valida o token e extrai reivindicações.
 */
class ValidateTokenUseCase(
    private val jwtService: JwtService,
    private val authMetrics: AuthMetrics? = null
) {
    private val logger = LoggerFactory.getLogger(ValidateTokenUseCase::class.java)
    
    fun execute(input: ValidateTokenInput): ValidateTokenOutput {
        return authMetrics?.validateTimer?.recordCallable<ValidateTokenOutput> {
            executeInternal(input)
        } ?: executeInternal(input)
    }

    private fun executeInternal(input: ValidateTokenInput): ValidateTokenOutput {
        logger.debug("[VALIDATE_TOKEN_USE_CASE] iniciando validação do token (tokenLength={})", input.token.length)

        val claims = try {
            jwtService.validateToken(input.token)
        } catch (ex: Exception) {
            logger.debug("[VALIDATE_TOKEN_USE_CASE] falha na validação do token", ex)
            authMetrics?.validateFailureCounter?.increment()
            throw ex
        }

        logger.debug(
            "[VALIDATE_TOKEN_USE_CASE] token validado com sucesso sub={} email={} role={}",
            claims["sub"],
            claims["email"],
            claims["role"]
        )
        authMetrics?.validateSuccessCounter?.increment()

        return ValidateTokenOutput(
            valid = true,
            userId = UUID.fromString(claims["sub"] as String),
            email = claims["email"] as String,
            role = claims["role"] as String
        )
    }
    
    data class ValidateTokenInput(
        val token: String
    )
    
    data class ValidateTokenOutput(
        val valid: Boolean,
        val userId: UUID,
        val email: String,
        val role: String
    )
}
