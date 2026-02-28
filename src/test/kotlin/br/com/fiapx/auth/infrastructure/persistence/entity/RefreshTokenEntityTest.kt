package br.com.fiapx.auth.infrastructure.persistence.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for RefreshTokenEntity.
 */
class RefreshTokenEntityTest {

    @Test
    fun `should create entity with all fields`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(3600)
        val createdAt = Instant.now()

        val entity = RefreshTokenEntity(
            id = id,
            userId = userId,
            token = "refresh-token-value",
            expiresAt = expiresAt,
            revoked = false,
            createdAt = createdAt
        )

        assertEquals(id, entity.id)
        assertEquals(userId, entity.userId)
        assertEquals("refresh-token-value", entity.token)
        assertEquals(expiresAt, entity.expiresAt)
        assertFalse(entity.revoked)
        assertEquals(createdAt, entity.createdAt)
    }

    @Test
    fun `should have revoked default to false`() {
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = "token",
            expiresAt = Instant.now().plusSeconds(3600)
        )

        assertFalse(entity.revoked)
    }

    @Test
    fun `should generate UUID id by default`() {
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = "token",
            expiresAt = Instant.now().plusSeconds(3600)
        )

        assertNotNull(entity.id)
    }

    @Test
    fun `should generate createdAt by default`() {
        val before = Instant.now()
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = "token",
            expiresAt = Instant.now().plusSeconds(3600)
        )
        val after = Instant.now()

        assertNotNull(entity.createdAt)
        assertTrue(!entity.createdAt.isBefore(before))
        assertTrue(!entity.createdAt.isAfter(after))
    }

    @Test
    fun `should create revoked entity`() {
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = "revoked-token",
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = true
        )

        assertTrue(entity.revoked)
    }

    @Test
    fun `should store token value correctly`() {
        val tokenValue = "a-very-long-refresh-token-string-that-is-unique"
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = tokenValue,
            expiresAt = Instant.now().plusSeconds(3600)
        )

        assertEquals(tokenValue, entity.token)
    }
}
