package br.com.fiapx.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import br.com.fiapx.auth.interfaces.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Ponto de entrada personalizado de autenticação para respostas não autorizadas 401.
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpServletResponse.SC_UNAUTHORIZED,
            error = "Unauthorized",
            message = authException.message ?: "Autenticação necessária",
            path = request.requestURI
        )
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
