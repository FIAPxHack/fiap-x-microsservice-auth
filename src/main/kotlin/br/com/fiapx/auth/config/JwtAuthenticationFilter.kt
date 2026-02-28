package br.com.fiapx.auth.config

import br.com.fiapx.auth.domain.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro de autenticação JWT.
 * Extrai e valida o token JWT do cabeçalho de autorização.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            
            try {
                val claims = jwtService.validateToken(token)

                val email = claims["email"] as String
                val role = claims["role"] as String

                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val authentication = UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                logger.debug("Validação do token falhou: ${e.message}")
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
