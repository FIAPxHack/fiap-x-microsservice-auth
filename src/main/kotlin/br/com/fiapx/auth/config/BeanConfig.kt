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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class BeanConfig {
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.access-token-expiration}")
    private var accessTokenExpiration: Long = 900000
    
    @Value("\${jwt.refresh-token-expiration}")
    private var refreshTokenExpiration: Long = 604800000
    
    @Value("\${jwt.issuer}")
    private lateinit var jwtIssuer: String
    
    @Value("\${user-service.base-url}")
    private lateinit var userServiceBaseUrl: String
    
    @Value("\${user-service.timeout:5000}")
    private var userServiceTimeout: Long = 5000
    
    @Bean
    fun jwtService(): JwtService {
        return JwtServiceImpl(
            secret = jwtSecret,
            accessTokenExpirationMs = accessTokenExpiration,
            issuer = jwtIssuer
        )
    }
    
    @Bean
    fun passwordService(): PasswordService {
        return PasswordServiceImpl()
    }
    
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(userServiceBaseUrl)
            .build()
    }
    
    @Bean
    fun loginUseCase(
        userService: UserService,
        passwordService: PasswordService,
        jwtService: JwtService,
        refreshTokenRepository: RefreshTokenRepository,
        loginAttemptRepository: LoginAttemptRepository
    ): LoginUseCase {
        return LoginUseCase(
            userService = userService,
            passwordService = passwordService,
            jwtService = jwtService,
            refreshTokenRepository = refreshTokenRepository,
            loginAttemptRepository = loginAttemptRepository,
            accessTokenExpirationMs = accessTokenExpiration,
            refreshTokenExpirationMs = refreshTokenExpiration
        )
    }
    
    @Bean
    fun validateTokenUseCase(jwtService: JwtService): ValidateTokenUseCase {
        return ValidateTokenUseCase(jwtService)
    }
    
    @Bean
    fun refreshTokenUseCase(
        refreshTokenRepository: RefreshTokenRepository,
        userService: UserService,
        jwtService: JwtService
    ): RefreshTokenUseCase {
        return RefreshTokenUseCase(
            refreshTokenRepository = refreshTokenRepository,
            userService = userService,
            jwtService = jwtService,
            refreshTokenExpirationMs = refreshTokenExpiration,
            enableTokenRotation = true
        )
    }
}
