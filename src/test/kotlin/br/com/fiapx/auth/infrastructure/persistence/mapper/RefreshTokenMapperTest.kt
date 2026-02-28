package br.com.fiapx.auth.infrastructure.persistence.mapper

import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.infrastructure.persistence.entity.RefreshTokenEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for RefreshTokenMapper.
 */
class RefreshTokenMapperTest {

    @Test
    fun `toDomain should map all fields from entity to domain`() {
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

        val domain = RefreshTokenMapper.toDomain(entity)

        assertEquals(id, domain.id)
        assertEquals(userId, domain.userId)
        assertEquals("refresh-token-value", domain.token)
        assertEquals(expiresAt, domain.expiresAt)
        assertFalse(domain.revoked)
        assertEquals(createdAt, domain.createdAt)
    }

    @Test
    fun `toDomain should map revoked entity`() {
        val entity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            token = "revoked-token",
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = true
        )

        val domain = RefreshTokenMapper.toDomain(entity)

        assertTrue(domain.revoked)
    }

    @Test
    fun `toEntity should map all fields from domain to entity`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(3600)
        val createdAt = Instant.now()

        val domain = RefreshToken(
            id = id,
            userId = userId,
            token = "domain-token",
            expiresAt = expiresAt,
            revoked = false,
            createdAt = createdAt
        )

        val entity = RefreshTokenMapper.toEntity(domain)

        assertEquals(id, entity.id)
        assertEquals(userId, entity.userId)
        assertEquals("domain-token", entity.token)
        assertEquals(expiresAt, entity.expiresAt)
        assertFalse(entity.revoked)
        assertEquals(createdAt, entity.createdAt)
    }

    @Test
    fun `toEntity should map revoked domain`() {
        val domain = RefreshToken(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            token = "revoked-domain-token",
            expiresAt = Instant.now().plusSeconds(3600),
            revoked = true,
            createdAt = Instant.now()
        )

        val entity = RefreshTokenMapper.toEntity(domain)

        assertTrue(entity.revoked)
    }

    @Test
    fun `roundtrip domain to entity and back should preserve data`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(7200)
        val createdAt = Instant.now()

        val original = RefreshToken(
            id = id,
            userId = userId,
            token = "roundtrip-token",
            expiresAt = expiresAt,
            revoked = false,
            createdAt = createdAt
        )

        val entity = RefreshTokenMapper.toEntity(original)
        val result = RefreshTokenMapper.toDomain(entity)

        assertEquals(original.id, result.id)
        assertEquals(original.userId, result.userId)
        assertEquals(original.token, result.token)
        assertEquals(original.expiresAt, result.expiresAt)
        assertEquals(original.revoked, result.revoked)
        assertEquals(original.createdAt, result.createdAt)
    }

    @Test
    fun `roundtrip entity to domain and back should preserve data`() {
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(1800)
        val createdAt = Instant.now().minusSeconds(100)

        val original = RefreshTokenEntity(
            id = id,
            userId = userId,
            token = "entity-roundtrip",
            expiresAt = expiresAt,
            revoked = true,
            createdAt = createdAt
        )

        val domain = RefreshTokenMapper.toDomain(original)
        val result = RefreshTokenMapper.toEntity(domain)

        assertEquals(original.id, result.id)
        assertEquals(original.userId, result.userId)
        assertEquals(original.token, result.token)
        assertEquals(original.expiresAt, result.expiresAt)
        assertEquals(original.revoked, result.revoked)
        assertEquals(original.createdAt, result.createdAt)
    }
}
