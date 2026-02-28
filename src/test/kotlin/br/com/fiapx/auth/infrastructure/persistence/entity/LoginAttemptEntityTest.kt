package br.com.fiapx.auth.infrastructure.persistence.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for LoginAttemptEntity.
 */
class LoginAttemptEntityTest {

    @Test
    fun `should create entity with all fields`() {
        val id = UUID.randomUUID()
        val now = Instant.now()

        val entity = LoginAttemptEntity(
            id = id,
            email = "user@example.com",
            success = true,
            ipAddress = "192.168.1.1",
            createdAt = now
        )

        assertEquals(id, entity.id)
        assertEquals("user@example.com", entity.email)
        assertTrue(entity.success)
        assertEquals("192.168.1.1", entity.ipAddress)
        assertEquals(now, entity.createdAt)
    }

    @Test
    fun `should create entity with null email`() {
        val entity = LoginAttemptEntity(
            email = null,
            success = false,
            ipAddress = "10.0.0.1"
        )

        assertNotNull(entity.id)
        assertNull(entity.email)
        assertFalse(entity.success)
        assertEquals("10.0.0.1", entity.ipAddress)
        assertNotNull(entity.createdAt)
    }

    @Test
    fun `should create entity with null ipAddress`() {
        val entity = LoginAttemptEntity(
            email = "user@example.com",
            success = true,
            ipAddress = null
        )

        assertNotNull(entity.id)
        assertEquals("user@example.com", entity.email)
        assertTrue(entity.success)
        assertNull(entity.ipAddress)
    }

    @Test
    fun `should generate UUID id by default`() {
        val entity = LoginAttemptEntity(
            email = "test@test.com",
            success = true,
            ipAddress = "127.0.0.1"
        )

        assertNotNull(entity.id)
    }

    @Test
    fun `should generate createdAt by default`() {
        val before = Instant.now()
        val entity = LoginAttemptEntity(
            email = "test@test.com",
            success = true,
            ipAddress = "127.0.0.1"
        )
        val after = Instant.now()

        assertNotNull(entity.createdAt)
        assertTrue(!entity.createdAt.isBefore(before))
        assertTrue(!entity.createdAt.isAfter(after))
    }

    @Test
    fun `should create entity for failed attempt`() {
        val entity = LoginAttemptEntity(
            email = "hacker@example.com",
            success = false,
            ipAddress = "1.2.3.4"
        )

        assertFalse(entity.success)
    }
}
