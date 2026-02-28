package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.service.JwtService
import java.util.UUID

/**
 * Valida o token e extrai reivindicações.
 */
class ValidateTokenUseCase(
    private val jwtService: JwtService
) {
    
    fun execute(input: ValidateTokenInput): ValidateTokenOutput {
        val claims = jwtService.validateToken(input.token)
        
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
