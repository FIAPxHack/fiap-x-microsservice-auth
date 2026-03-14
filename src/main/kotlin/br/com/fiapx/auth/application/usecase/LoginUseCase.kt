package br.com.fiapx.auth.application.usecase

import br.com.fiapx.auth.config.AuthMetrics
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
import org.slf4j.LoggerFactory

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
    private val refreshTokenExpirationMs: Long,
    private val authMetrics: AuthMetrics? = null
) {
    private val logger = LoggerFactory.getLogger(LoginUseCase::class.java)

    fun execute(input: LoginInput): LoginOutput {
        return authMetrics?.loginTimer?.recordCallable<LoginOutput> {
            executeInternal(input)
        } ?: executeInternal(input)
    }

    private fun executeInternal(input: LoginInput): LoginOutput {
        logger.debug("[LOGIN_USE_CASE] iniciando execução - email={} ip={}", input.email, input.ipAddress)

        val user = userService.findByEmail(input.email)
        logger.debug("[LOGIN_USE_CASE] userService.findByEmail retornou id={} for email={}", user.id, input.email)

        val passwordMatches = passwordService.matches(input.password, user.passwordHash)
        logger.debug("[LOGIN_USE_CASE] Verificação de correspondência de senha para email={} result={}", input.email, passwordMatches)

        val loginAttempt = LoginAttempt(
            id = UUID.randomUUID(),
            email = input.email,
            success = passwordMatches,
            ipAddress = input.ipAddress,
            createdAt = Instant.now()
        )
        loginAttemptRepository.save(loginAttempt)
        logger.debug("[LOGIN_USE_CASE] Tentativa de login salva id={} email={} success={}", loginAttempt.id, loginAttempt.email, loginAttempt.success)

        if (!passwordMatches) {
            logger.debug("[LOGIN_USE_CASE] Credenciais inválidas para email={}", input.email)
            authMetrics?.loginFailureCounter?.increment()
            throw InvalidCredentialsException("[LOGIN_USE_CASE] E-mail ou senha inválidos")
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role)
        logger.debug("[LOGIN_USE_CASE] Token de acesso gerado para userId={}", user.id)

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
        logger.debug("[LOGIN_USE_CASE] Token de atualização salvo id={} userId={}", refreshToken.id, refreshToken.userId)
        authMetrics?.loginSuccessCounter?.increment()

        logger.debug("[LOGIN_USE_CASE] Login bem-sucedido for email={} userId={}", input.email, user.id)
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
