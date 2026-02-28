package br.com.fiapx.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for AuthServiceApplication.
 */
class AuthServiceApplicationTest {

    @Test
    fun `AuthServiceApplication class should be instantiable`() {
        val app = AuthServiceApplication()
        assertNotNull(app)
    }

    @Test
    fun `AuthServiceApplication should have SpringBootApplication annotation`() {
        val annotation = AuthServiceApplication::class.java.getAnnotation(
            org.springframework.boot.autoconfigure.SpringBootApplication::class.java
        )
        assertNotNull(annotation)
    }
}
