package br.com.fiapx.auth.interfaces.controller

import br.com.fiapx.auth.application.usecase.LoginUseCase
import br.com.fiapx.auth.application.usecase.RefreshTokenUseCase
import br.com.fiapx.auth.application.usecase.ValidateTokenUseCase
import br.com.fiapx.auth.interfaces.dto.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) {
    
    /**
     * Endpoint de login.
     * Autentica o usuário e retorna tokens de acesso e atualização.
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<LoginResponse> {
        val input = LoginUseCase.LoginInput(
            email = request.email,
            password = request.password,
            ipAddress = getClientIp(httpRequest)
        )
        
        val output = loginUseCase.execute(input)
        
        val response = LoginResponse(
            accessToken = output.accessToken,
            refreshToken = output.refreshToken,
            tokenType = output.tokenType,
            expiresIn = output.expiresIn
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * Validar o endpoint do token.
     * Valida um token de acesso JWT e retorna informações do usuário.
     */
    @PostMapping("/validate")
    fun validateToken(
        @Valid @RequestBody request: ValidateTokenRequest
    ): ResponseEntity<ValidateTokenResponse> {
        val input = ValidateTokenUseCase.ValidateTokenInput(
            token = request.token
        )
        
        val output = validateTokenUseCase.execute(input)
        
        val response = ValidateTokenResponse(
            valid = output.valid,
            userId = output.userId.toString(),
            email = output.email,
            role = output.role
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * Atualizar o endpoint do token.
     * Atualiza um token de acesso usando um token de atualização válido.
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<RefreshTokenResponse> {
        val input = RefreshTokenUseCase.RefreshTokenInput(
            refreshToken = request.refreshToken
        )
        
        val output = refreshTokenUseCase.execute(input)
        
        val response = RefreshTokenResponse(
            accessToken = output.accessToken,
            refreshToken = output.refreshToken,
            tokenType = output.tokenType
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * Extrai o endereço IP do cliente da solicitação.
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (xForwardedFor != null && xForwardedFor.isNotBlank()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }
}
