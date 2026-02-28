package br.com.fiapx.auth.config

import br.com.fiapx.auth.domain.service.JwtService
import io.mockk.*
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

/**
 * Unit tests for JwtAuthenticationFilter.
 */
class JwtAuthenticationFilterTest {

    private lateinit var jwtService: JwtService
    private lateinit var filter: JwtAuthenticationFilter

    @BeforeEach
    fun setup() {
        jwtService = mockk()
        filter = JwtAuthenticationFilter(jwtService)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        clearAllMocks()
    }

    @Test
    fun `should set authentication when valid Bearer token is present`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()
        val userId = UUID.randomUUID()

        request.addHeader("Authorization", "Bearer valid.jwt.token")
        every { jwtService.validateToken("valid.jwt.token") } returns mapOf(
            "sub" to userId.toString(),
            "email" to "user@example.com",
            "role" to "USER"
        )

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals("user@example.com", authentication!!.principal)
        assertTrue(authentication.authorities.any { it.authority == "ROLE_USER" })
    }

    @Test
    fun `should not set authentication when no Authorization header`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { jwtService.validateToken(any()) }
    }

    @Test
    fun `should not set authentication when Authorization header does not start with Bearer`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        request.addHeader("Authorization", "Basic dXNlcjpwYXNz")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { jwtService.validateToken(any()) }
    }

    @Test
    fun `should not set authentication when token validation fails`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        request.addHeader("Authorization", "Bearer invalid.token")
        every { jwtService.validateToken("invalid.token") } throws RuntimeException("Invalid token")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `should always continue filter chain regardless of token validity`() {
        // Given
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val filterChain = mockk<FilterChain>()

        request.addHeader("Authorization", "Bearer bad-token")
        every { jwtService.validateToken("bad-token") } throws RuntimeException("fail")
        every { filterChain.doFilter(request, response) } just Runs

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 1) { filterChain.doFilter(request, response) }
    }
}
