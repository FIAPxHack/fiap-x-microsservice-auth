package br.com.fiapx.auth.infrastructure.persistence.mapper

import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.infrastructure.persistence.entity.LoginAttemptEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for LoginAttemptMapper.
 */
class LoginAttemptMapperTest {

    @Test
    fun `toDomain should map all fields from entity to domain`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val entity = LoginAttemptEntity(
            id = id,
            email = "user@example.com",
            success = true,
            ipAddress = "192.168.1.1",
            createdAt = now
        )

        val domain = LoginAttemptMapper.toDomain(entity)

        assertEquals(id, domain.id)
        assertEquals("user@example.com", domain.email)
        assertTrue(domain.success)
        assertEquals("192.168.1.1", domain.ipAddress)
        assertEquals(now, domain.createdAt)
    }

    @Test
    fun `toDomain should handle null email`() {
        val entity = LoginAttemptEntity(
            email = null,
            success = false,
            ipAddress = "10.0.0.1"
        )

        val domain = LoginAttemptMapper.toDomain(entity)

        assertNull(domain.email)
        assertFalse(domain.success)
    }

    @Test
    fun `toDomain should handle null ipAddress`() {
        val entity = LoginAttemptEntity(
            email = "test@test.com",
            success = true,
            ipAddress = null
        )

        val domain = LoginAttemptMapper.toDomain(entity)

        assertNull(domain.ipAddress)
    }

    @Test
    fun `toEntity should map all fields from domain to entity`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val domain = LoginAttempt(
            id = id,
            email = "user@example.com",
            success = true,
            ipAddress = "192.168.1.1",
            createdAt = now
        )

        val entity = LoginAttemptMapper.toEntity(domain)

        assertEquals(id, entity.id)
        assertEquals("user@example.com", entity.email)
        assertTrue(entity.success)
        assertEquals("192.168.1.1", entity.ipAddress)
        assertEquals(now, entity.createdAt)
    }

    @Test
    fun `toEntity should handle null email`() {
        val domain = LoginAttempt(
            id = UUID.randomUUID(),
            email = null,
            success = false,
            ipAddress = "127.0.0.1",
            createdAt = Instant.now()
        )

        val entity = LoginAttemptMapper.toEntity(domain)

        assertNull(entity.email)
        assertFalse(entity.success)
    }

    @Test
    fun `toEntity should handle null ipAddress`() {
        val domain = LoginAttempt(
            id = UUID.randomUUID(),
            email = "test@test.com",
            success = true,
            ipAddress = null,
            createdAt = Instant.now()
        )

        val entity = LoginAttemptMapper.toEntity(domain)

        assertNull(entity.ipAddress)
    }

    @Test
    fun `roundtrip domain to entity and back should preserve data`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val original = LoginAttempt(
            id = id,
            email = "roundtrip@test.com",
            success = true,
            ipAddress = "172.16.0.1",
            createdAt = now
        )

        val entity = LoginAttemptMapper.toEntity(original)
        val result = LoginAttemptMapper.toDomain(entity)

        assertEquals(original.id, result.id)
        assertEquals(original.email, result.email)
        assertEquals(original.success, result.success)
        assertEquals(original.ipAddress, result.ipAddress)
        assertEquals(original.createdAt, result.createdAt)
    }
}
