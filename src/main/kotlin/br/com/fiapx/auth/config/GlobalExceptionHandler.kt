package br.com.fiapx.auth.config

import br.com.fiapx.auth.domain.exception.*
import br.com.fiapx.auth.interfaces.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        ex: InvalidCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "[Unauthorized]",
            message = ex.message ?: "Credenciais inválidas",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }
    
    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpired(
        ex: TokenExpiredException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "[Unauthorized]",
            message = ex.message ?: "Token expirou",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }
    
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(
        ex: InvalidTokenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "[Unauthorized]",
            message = ex.message ?: "Token inválido",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }
    
    @ExceptionHandler(TokenRevokedException::class)
    fun handleTokenRevoked(
        ex: TokenRevokedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "[Unauthorized]",
            message = ex.message ?: "O token foi revogado",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }
    
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "[Not Found]",
            message = ex.message ?: "Usuário não encontrado",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }
    
    @ExceptionHandler(UserServiceException::class)
    fun handleUserServiceException(
        ex: UserServiceException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            error = "[SERVIÇO_INDISPONÍVEL]",
            message = ex.message ?: "Serviço ao usuário indisponível",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error)
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.allErrors
            .joinToString(", ") { error ->
                when (error) {
                    is FieldError -> "${error.field}: ${error.defaultMessage}"
                    else -> error.defaultMessage ?: "Validation error"
                }
            }
        
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "Um erro inesperado ocorreu",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
