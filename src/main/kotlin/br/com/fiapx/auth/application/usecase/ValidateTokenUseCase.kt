package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.config.AuthMetrics
import br.com.fiapx.auth.domain.service.JwtService
import java.util.UUID

/**
 * Valida o token e extrai reivindicações.
 */
class ValidateTokenUseCase(
    private val jwtService: JwtService,
    private val authMetrics: AuthMetrics? = null
) {
    
    fun execute(input: ValidateTokenInput): ValidateTokenOutput {
        return authMetrics?.validateTimer?.recordCallable<ValidateTokenOutput> {
            executeInternal(input)
        } ?: executeInternal(input)
    }

    private fun executeInternal(input: ValidateTokenInput): ValidateTokenOutput {
        val claims = try {
            jwtService.validateToken(input.token)
        } catch (ex: Exception) {
            authMetrics?.validateFailureCounter?.increment()
            throw ex
        }

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
