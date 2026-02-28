package br.com.fiapx.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import br.com.fiapx.auth.interfaces.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Handler de acesso negado personalizado para respostas 403 proibidas.
 */
@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {
    
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpServletResponse.SC_FORBIDDEN,
            error = "Forbidden",
            message = accessDeniedException.message ?: "Acesso negado",
            path = request.requestURI
        )
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
