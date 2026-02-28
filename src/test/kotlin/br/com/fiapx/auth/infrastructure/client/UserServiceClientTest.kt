package br.com.fiapx.auth.infrastructure.client

import br.com.fiapx.auth.domain.exception.UserNotFoundException
import br.com.fiapx.auth.domain.exception.UserServiceException
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Unit tests for UserServiceClient.
 */
class UserServiceClientTest {

    private lateinit var webClient: WebClient
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var responseSpec: WebClient.ResponseSpec
    private lateinit var userServiceClient: UserServiceClient

    @BeforeEach
    fun setup() {
        webClient = mockk()
        requestHeadersUriSpec = mockk()
        requestHeadersSpec = mockk()
        responseSpec = mockk()
        userServiceClient = UserServiceClient(webClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `findByEmail should throw UserNotFoundException when WebClientResponseException NotFound occurs`() {
        // Given
        val email = "unknown@example.com"
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws WebClientResponseException.create(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            HttpHeaders.EMPTY,
            ByteArray(0),
            StandardCharsets.UTF_8
        )

        // When & Then
        assertThrows<UserNotFoundException> {
            userServiceClient.findByEmail(email)
        }
    }

    @Test
    fun `findByEmail should throw UserServiceException on generic error`() {
        // Given
        val email = "user@example.com"
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws RuntimeException("Connection refused")

        // When & Then
        val exception = assertThrows<UserServiceException> {
            userServiceClient.findByEmail(email)
        }
        assertTrue(exception.message!!.contains("Erro ao comunicar com o Serviço do Usuário"))
    }

    @Test
    fun `findById should throw UserNotFoundException when WebClientResponseException NotFound occurs`() {
        // Given
        val id = UUID.randomUUID()
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<UUID>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws WebClientResponseException.create(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            HttpHeaders.EMPTY,
            ByteArray(0),
            StandardCharsets.UTF_8
        )

        // When & Then
        assertThrows<UserNotFoundException> {
            userServiceClient.findById(id)
        }
    }

    @Test
    fun `findById should throw UserServiceException on generic error`() {
        // Given
        val id = UUID.randomUUID()
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<UUID>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws RuntimeException("Timeout")

        // When & Then
        val exception = assertThrows<UserServiceException> {
            userServiceClient.findById(id)
        }
        assertTrue(exception.message!!.contains("Erro ao comunicar com o Serviço do Usuário"))
    }

    @Test
    fun `findByEmail should propagate UserNotFoundException directly`() {
        // Given
        val email = "notfound@example.com"
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws UserNotFoundException("Usuário com email [$email] não encontrado")

        // When & Then
        val exception = assertThrows<UserNotFoundException> {
            userServiceClient.findByEmail(email)
        }
        assertTrue(exception.message!!.contains(email))
    }

    @Test
    fun `findById should propagate UserNotFoundException directly`() {
        // Given
        val id = UUID.randomUUID()
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<UUID>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws UserNotFoundException("Usuário com ID [$id] não encontrado")

        // When & Then
        val exception = assertThrows<UserNotFoundException> {
            userServiceClient.findById(id)
        }
        assertTrue(exception.message!!.contains(id.toString()))
    }
}
