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
import org.springframework.security.access.AccessDeniedException
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Unit tests for JwtAccessDeniedHandler.
 */
class JwtAccessDeniedHandlerTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var handler: JwtAccessDeniedHandler

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper().registerKotlinModule()
        handler = JwtAccessDeniedHandler(objectMapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `handle should set 403 status and write JSON error response`() {
        // Given
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        val accessDeniedException = AccessDeniedException("Access is denied")

        every { request.requestURI } returns "/auth/admin"
        every { response.status = HttpServletResponse.SC_FORBIDDEN } just Runs
        every { response.contentType = MediaType.APPLICATION_JSON_VALUE } just Runs
        every { response.writer } returns printWriter

        // When
        handler.handle(request, response, accessDeniedException)

        // Then
        verify { response.status = HttpServletResponse.SC_FORBIDDEN }
        verify { response.contentType = MediaType.APPLICATION_JSON_VALUE }

        val json = stringWriter.toString()
        val errorResponse = objectMapper.readValue(json, ErrorResponse::class.java)
        assertEquals(403, errorResponse.status)
        assertEquals("Forbidden", errorResponse.error)
        assertEquals("Access is denied", errorResponse.message)
        assertEquals("/auth/admin", errorResponse.path)
        assertNotNull(errorResponse.timestamp)
    }

    @Test
    fun `handle should use default message when exception message is null`() {
        // Given
        val request = mockk<HttpServletRequest>()
        val response = mockk<HttpServletResponse>()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        val accessDeniedException = mockk<AccessDeniedException>()

        every { accessDeniedException.message } returns null
        every { request.requestURI } returns "/auth/test"
        every { response.status = HttpServletResponse.SC_FORBIDDEN } just Runs
        every { response.contentType = MediaType.APPLICATION_JSON_VALUE } just Runs
        every { response.writer } returns printWriter

        // When
        handler.handle(request, response, accessDeniedException)

        // Then
        val json = stringWriter.toString()
        val errorResponse = objectMapper.readValue(json, ErrorResponse::class.java)
        assertEquals("Acesso negado", errorResponse.message)
    }
}
