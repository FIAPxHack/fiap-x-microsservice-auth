package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.infrastructure.persistence.entity.RefreshTokenEntity
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for RefreshTokenRepositoryImpl.
 */
class RefreshTokenRepositoryImplTest {

    private lateinit var jpaRepository: JpaRefreshTokenRepository
    private lateinit var repository: RefreshTokenRepositoryImpl

    @BeforeEach
    fun setup() {
        jpaRepository = mockk()
        repository = RefreshTokenRepositoryImpl(jpaRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `save should convert domain to entity, save, and return domain`() {
        // Given
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(3600)
        val createdAt = Instant.now()

        val refreshToken = RefreshToken(
            id = id,
            userId = userId,
            token = "refresh-token",
            expiresAt = expiresAt,
            revoked = false,
            createdAt = createdAt
        )

        val savedEntity = RefreshTokenEntity(
            id = id,
            userId = userId,
            token = "refresh-token",
            expiresAt = expiresAt,
            revoked = false,
            createdAt = createdAt
        )

        every { jpaRepository.save(any()) } returns savedEntity

        // When
        val result = repository.save(refreshToken)

        // Then
        assertNotNull(result)
        assertEquals(id, result.id)
        assertEquals(userId, result.userId)
        assertEquals("refresh-token", result.token)
        assertEquals(expiresAt, result.expiresAt)
        assertFalse(result.revoked)

        verify(exactly = 1) { jpaRepository.save(any()) }
    }

    @Test
    fun `findByToken should return domain object when found`() {
        // Given
        val id = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val expiresAt = Instant.now().plusSeconds(3600)
        val entity = RefreshTokenEntity(
            id = id,
            userId = userId,
            token = "existing-token",
            expiresAt = expiresAt,
            revoked = false
        )

        every { jpaRepository.findByToken("existing-token") } returns entity

        // When
        val result = repository.findByToken("existing-token")

        // Then
        assertNotNull(result)
        assertEquals(id, result!!.id)
        assertEquals(userId, result.userId)
        assertEquals("existing-token", result.token)

        verify(exactly = 1) { jpaRepository.findByToken("existing-token") }
    }

    @Test
    fun `findByToken should return null when not found`() {
        // Given
        every { jpaRepository.findByToken("non-existent") } returns null

        // When
        val result = repository.findByToken("non-existent")

        // Then
        assertNull(result)
    }

    @Test
    fun `findByUserId should return list of domain objects`() {
        // Given
        val userId = UUID.randomUUID()
        val entity1 = RefreshTokenEntity(
            userId = userId,
            token = "token-1",
            expiresAt = Instant.now().plusSeconds(3600)
        )
        val entity2 = RefreshTokenEntity(
            userId = userId,
            token = "token-2",
            expiresAt = Instant.now().plusSeconds(7200)
        )

        every { jpaRepository.findByUserId(userId) } returns listOf(entity1, entity2)

        // When
        val result = repository.findByUserId(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals("token-1", result[0].token)
        assertEquals("token-2", result[1].token)

        verify(exactly = 1) { jpaRepository.findByUserId(userId) }
    }

    @Test
    fun `findByUserId should return empty list when no tokens found`() {
        // Given
        val userId = UUID.randomUUID()
        every { jpaRepository.findByUserId(userId) } returns emptyList()

        // When
        val result = repository.findByUserId(userId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `revokeAllByUserId should delegate to jpa repository`() {
        // Given
        val userId = UUID.randomUUID()
        every { jpaRepository.revokeAllByUserId(userId) } just Runs

        // When
        repository.revokeAllByUserId(userId)

        // Then
        verify(exactly = 1) { jpaRepository.revokeAllByUserId(userId) }
    }

    @Test
    fun `deleteById should delegate to jpa repository`() {
        // Given
        val id = UUID.randomUUID()
        every { jpaRepository.deleteById(id) } just Runs

        // When
        repository.deleteById(id)

        // Then
        verify(exactly = 1) { jpaRepository.deleteById(id) }
    }
}
