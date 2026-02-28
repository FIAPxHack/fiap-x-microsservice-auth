package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.domain.exception.InvalidCredentialsException
import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.repository.LoginAttemptRepository
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.domain.service.JwtService
import br.com.fiapx.auth.domain.service.PasswordService
import br.com.fiapx.auth.domain.service.UserService
import java.time.Instant
import java.util.UUID

/**
 * Use case for user login.
 * Valida credenciais e gera tokens de acesso e atualização.
 */
class LoginUseCase(
    private val userService: UserService,
    private val passwordService: PasswordService,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val loginAttemptRepository: LoginAttemptRepository,
    private val accessTokenExpirationMs: Long,
    private val refreshTokenExpirationMs: Long
) {
    
    fun execute(input: LoginInput): LoginOutput {
        val user = userService.findByEmail(input.email)

        val passwordMatches = passwordService.matches(input.password, user.passwordHash)

        val loginAttempt = LoginAttempt(
            id = UUID.randomUUID(),
            email = input.email,
            success = passwordMatches,
            ipAddress = input.ipAddress,
            createdAt = Instant.now()
        )
        loginAttemptRepository.save(loginAttempt)

        if (!passwordMatches) {
            throw InvalidCredentialsException("[LOGIN_USE_CASE] E-mail ou senha inválidos")
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role)

        val refreshTokenValue = jwtService.generateRefreshToken()
        val refreshToken = RefreshToken(
            id = UUID.randomUUID(),
            userId = user.id,
            token = refreshTokenValue,
            expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs),
            revoked = false,
            createdAt = Instant.now()
        )
        refreshTokenRepository.save(refreshToken)
        
        return LoginOutput(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            tokenType = "Bearer",
            expiresIn = accessTokenExpirationMs / 1000 // Convert to seconds
        )
    }
    
    data class LoginInput(
        val email: String,
        val password: String,
        val ipAddress: String?
    )
    
    data class LoginOutput(
        val accessToken: String,
        val refreshToken: String,
        val tokenType: String,
        val expiresIn: Long
    )
}
