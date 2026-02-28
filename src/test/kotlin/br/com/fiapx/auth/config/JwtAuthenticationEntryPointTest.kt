package br.com.fiapx.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import br.com.fiapx.auth.interfaces.dto.ErrorResponse
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Unit tests for JwtAuthenticationEntryPoint.
 */
class JwtAuthenticationEntryPointTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var entryPoint: JwtAuthenticationEntryPoint

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper().registerKotlinModule()
        entryPoint = JwtAuthenticationEntryPoint(objectMapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `commence should set 401 status and write JSON error response`() {
        // Given
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        val authException = mockk<AuthenticationException>()

        every { authException.message } returns "Full authentication is required"
        every { request.requestURI } returns "/auth/validate"
        every { response.status = HttpServletResponse.SC_UNAUTHORIZED } just Runs
        every { response.contentType = MediaType.APPLICATION_JSON_VALUE } just Runs
        every { response.writer } returns printWriter

        // When
        entryPoint.commence(request, response, authException)

        // Then
        verify { response.status = HttpServletResponse.SC_UNAUTHORIZED }
        verify { response.contentType = MediaType.APPLICATION_JSON_VALUE }

        val json = stringWriter.toString()
        val errorResponse = objectMapper.readValue(json, ErrorResponse::class.java)
        assertEquals(401, errorResponse.status)
        assertEquals("Unauthorized", errorResponse.error)
        assertEquals("Full authentication is required", errorResponse.message)
        assertEquals("/auth/validate", errorResponse.path)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `commence should use default message when exception message is null`() {
        // Given
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        val authException = mockk<AuthenticationException>()

        every { authException.message } returns null
        every { request.requestURI } returns "/auth/test"
        every { response.status = HttpServletResponse.SC_UNAUTHORIZED } just Runs
        every { response.contentType = MediaType.APPLICATION_JSON_VALUE } just Runs
        every { response.writer } returns printWriter

        // When
        entryPoint.commence(request, response, authException)

        // Then
        val json = stringWriter.toString()
        val errorResponse = objectMapper.readValue(json, ErrorResponse::class.java)
        assertEquals("Autenticação necessária", errorResponse.message)
    }
}
