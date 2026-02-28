package br.com.fiapx.auth.config

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

/**
 * Unit tests for SecurityConfig.
 */
class SecurityConfigTest {

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `SecurityConfig class should have EnableWebSecurity annotation`() {
        val annotation = SecurityConfig::class.java.getAnnotation(EnableWebSecurity::class.java)
        assertNotNull(annotation)
    }

    @Test
    fun `SecurityConfig class should have EnableMethodSecurity annotation with prePostEnabled`() {
        val annotation = SecurityConfig::class.java.getAnnotation(EnableMethodSecurity::class.java)
        assertNotNull(annotation)
        assertTrue(annotation.prePostEnabled)
    }

    @Test
    fun `SecurityConfig class should have Configuration annotation`() {
        val annotation = SecurityConfig::class.java.getAnnotation(
            org.springframework.context.annotation.Configuration::class.java
        )
        assertNotNull(annotation)
    }

    @Test
    fun `SecurityConfig should be instantiable with dependencies`() {
        val jwtAuthFilter = mockk<JwtAuthenticationFilter>()
        val entryPoint = mockk<JwtAuthenticationEntryPoint>()
        val accessDeniedHandler = mockk<JwtAccessDeniedHandler>()

        val config = SecurityConfig(jwtAuthFilter, entryPoint, accessDeniedHandler)
        assertNotNull(config)
    }
}
