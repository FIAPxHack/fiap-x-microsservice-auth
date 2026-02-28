package br.com.fiapx.auth.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for OpenApiConfig.
 */
class OpenApiConfigTest {

    private lateinit var openApiConfig: OpenApiConfig

    @BeforeEach
    fun setup() {
        openApiConfig = OpenApiConfig()
    }

    @Test
    fun `openAPI should return non-null OpenAPI object`() {
        val openAPI = openApiConfig.openAPI()
        assertNotNull(openAPI)
    }

    @Test
    fun `openAPI should have correct title`() {
        val openAPI = openApiConfig.openAPI()
        assertEquals("FIAP-X Authentication Service API", openAPI.info.title)
    }

    @Test
    fun `openAPI should have correct description`() {
        val openAPI = openApiConfig.openAPI()
        assertEquals("Authentication microservice with JWT support", openAPI.info.description)
    }

    @Test
    fun `openAPI should have correct version`() {
        val openAPI = openApiConfig.openAPI()
        assertEquals("1.0.0", openAPI.info.version)
    }

    @Test
    fun `openAPI should have MIT license`() {
        val openAPI = openApiConfig.openAPI()
        assertNotNull(openAPI.info.license)
        assertEquals("MIT", openAPI.info.license.name)
    }

    @Test
    fun `openAPI should have Bearer Authentication security scheme`() {
        val openAPI = openApiConfig.openAPI()
        assertNotNull(openAPI.components)
        assertNotNull(openAPI.components.securitySchemes)
        val scheme = openAPI.components.securitySchemes["Bearer Authentication"]
        assertNotNull(scheme)
        assertEquals(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP, scheme!!.type)
        assertEquals("bearer", scheme.scheme)
        assertEquals("JWT", scheme.bearerFormat)
        assertEquals("Enter JWT token", scheme.description)
    }

    @Test
    fun `openAPI should have security requirement`() {
        val openAPI = openApiConfig.openAPI()
        assertNotNull(openAPI.security)
        assertTrue(openAPI.security.isNotEmpty())
        assertTrue(openAPI.security.any { it.containsKey("Bearer Authentication") })
    }
}
