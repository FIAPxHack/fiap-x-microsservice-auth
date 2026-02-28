package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.infrastructure.persistence.entity.LoginAttemptEntity
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * Unit tests for LoginAttemptRepositoryImpl.
 */
class LoginAttemptRepositoryImplTest {

    private lateinit var jpaRepository: JpaLoginAttemptRepository
    private lateinit var repository: LoginAttemptRepositoryImpl

    @BeforeEach
    fun setup() {
        jpaRepository = mockk()
        repository = LoginAttemptRepositoryImpl(jpaRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `save should convert domain to entity, save, and return domain`() {
        // Given
        val id = UUID.randomUUID()
        val now = Instant.now()
        val loginAttempt = LoginAttempt(
            id = id,
            email = "user@example.com",
            success = true,
            ipAddress = "192.168.1.1",
            createdAt = now
        )

        val savedEntity = LoginAttemptEntity(
            id = id,
            email = "user@example.com",
            success = true,
            ipAddress = "192.168.1.1",
            createdAt = now
        )

        every { jpaRepository.save(any()) } returns savedEntity

        // When
        val result = repository.save(loginAttempt)

        // Then
        assertNotNull(result)
        assertEquals(id, result.id)
        assertEquals("user@example.com", result.email)
        assertTrue(result.success)
        assertEquals("192.168.1.1", result.ipAddress)

        verify(exactly = 1) { jpaRepository.save(any()) }
    }

    @Test
    fun `findByEmail should return list of domain objects`() {
        // Given
        val email = "user@example.com"
        val entity1 = LoginAttemptEntity(
            email = email,
            success = true,
            ipAddress = "10.0.0.1"
        )
        val entity2 = LoginAttemptEntity(
            email = email,
            success = false,
            ipAddress = "10.0.0.2"
        )

        every { jpaRepository.findByEmail(email) } returns listOf(entity1, entity2)

        // When
        val result = repository.findByEmail(email)

        // Then
        assertEquals(2, result.size)
        assertEquals(email, result[0].email)
        assertTrue(result[0].success)
        assertEquals(email, result[1].email)
        assertFalse(result[1].success)

        verify(exactly = 1) { jpaRepository.findByEmail(email) }
    }

    @Test
    fun `findByEmail should return empty list when no attempts found`() {
        // Given
        every { jpaRepository.findByEmail("unknown@example.com") } returns emptyList()

        // When
        val result = repository.findByEmail("unknown@example.com")

        // Then
        assertTrue(result.isEmpty())
    }
}
