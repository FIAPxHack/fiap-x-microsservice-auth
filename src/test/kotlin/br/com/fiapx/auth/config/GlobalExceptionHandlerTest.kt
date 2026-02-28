package br.com.fiapx.auth.config

import br.com.fiapx.auth.domain.exception.*
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.core.MethodParameter

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private lateinit var handler: GlobalExceptionHandler
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setup() {
        handler = GlobalExceptionHandler()
        request = mockk()
        every { request.requestURI } returns "/auth/login"
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `handleInvalidCredentials should return 401 UNAUTHORIZED`() {
        val ex = InvalidCredentialsException("Credenciais inválidas")

        val response = handler.handleInvalidCredentials(ex, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(401, response.body!!.status)
        assertEquals("Unauthorized", response.body!!.error)
        assertEquals("Credenciais inválidas", response.body!!.message)
        assertEquals("/auth/login", response.body!!.path)
    }

    @Test
    fun `handleInvalidCredentials should use default message when null`() {
        val ex = mockk<InvalidCredentialsException>()
        every { ex.message } returns null

        val response = handler.handleInvalidCredentials(ex, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Credenciais inválidas", response.body!!.message)
    }

    @Test
    fun `handleTokenExpired should return 401 UNAUTHORIZED`() {
        val ex = TokenExpiredException("Token expirou")

        val response = handler.handleTokenExpired(ex, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(401, response.body!!.status)
        assertEquals("Unauthorized", response.body!!.error)
        assertEquals("Token expirou", response.body!!.message)
    }

    @Test
    fun `handleTokenExpired should use default message when null`() {
        val ex = mockk<TokenExpiredException>()
        every { ex.message } returns null

        val response = handler.handleTokenExpired(ex, request)

        assertEquals("Token expirou", response.body!!.message)
    }

    @Test
    fun `handleInvalidToken should return 401 UNAUTHORIZED`() {
        val ex = InvalidTokenException("Token inválido")

        val response = handler.handleInvalidToken(ex, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(401, response.body!!.status)
        assertEquals("Unauthorized", response.body!!.error)
        assertEquals("Token inválido", response.body!!.message)
    }

    @Test
    fun `handleInvalidToken should use default message when null`() {
        val ex = mockk<InvalidTokenException>()
        every { ex.message } returns null

        val response = handler.handleInvalidToken(ex, request)

        assertEquals("Token inválido", response.body!!.message)
    }

    @Test
    fun `handleTokenRevoked should return 401 UNAUTHORIZED`() {
        val ex = TokenRevokedException("O token foi revogado")

        val response = handler.handleTokenRevoked(ex, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(401, response.body!!.status)
        assertEquals("Unauthorized", response.body!!.error)
        assertEquals("O token foi revogado", response.body!!.message)
    }

    @Test
    fun `handleTokenRevoked should use default message when null`() {
        val ex = mockk<TokenRevokedException>()
        every { ex.message } returns null

        val response = handler.handleTokenRevoked(ex, request)

        assertEquals("O token foi revogado", response.body!!.message)
    }

    @Test
    fun `handleUserNotFound should return 404 NOT_FOUND`() {
        val ex = UserNotFoundException("Usuário não encontrado")

        val response = handler.handleUserNotFound(ex, request)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals(404, response.body!!.status)
        assertEquals("[Not Found]", response.body!!.error)
        assertEquals("Usuário não encontrado", response.body!!.message)
    }

    @Test
    fun `handleUserNotFound should use default message when null`() {
        val ex = mockk<UserNotFoundException>()
        every { ex.message } returns null

        val response = handler.handleUserNotFound(ex, request)

        assertEquals("Usuário não encontrado", response.body!!.message)
    }

    @Test
    fun `handleUserServiceException should return 503 SERVICE_UNAVAILABLE`() {
        val ex = UserServiceException("Serviço ao usuário indisponível")

        val response = handler.handleUserServiceException(ex, request)

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertNotNull(response.body)
        assertEquals(503, response.body!!.status)
        assertEquals("[SERVIÇO_INDISPONÍVEL]", response.body!!.error)
        assertEquals("Serviço ao usuário indisponível", response.body!!.message)
    }

    @Test
    fun `handleUserServiceException should use default message when null`() {
        val ex = mockk<UserServiceException>()
        every { ex.message } returns null

        val response = handler.handleUserServiceException(ex, request)

        assertEquals("Serviço ao usuário indisponível", response.body!!.message)
    }

    @Test
    fun `handleValidationException should return 400 BAD_REQUEST with field errors`() {
        val bindingResult = BeanPropertyBindingResult(Any(), "target")
        bindingResult.addError(FieldError("target", "email", "O e-mail deve ser válido"))
        bindingResult.addError(FieldError("target", "password", "A senha é necessária"))

        val methodParam = mockk<MethodParameter>()
        val ex = MethodArgumentNotValidException(methodParam, bindingResult)

        val response = handler.handleValidationException(ex, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body!!.status)
        assertEquals("Bad Request", response.body!!.error)
        assertTrue(response.body!!.message.contains("email"))
        assertTrue(response.body!!.message.contains("password"))
    }

    @Test
    fun `handleGenericException should return 500 INTERNAL_SERVER_ERROR`() {
        val ex = RuntimeException("Algo inesperado aconteceu")

        val response = handler.handleGenericException(ex, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body!!.status)
        assertEquals("Internal Server Error", response.body!!.error)
        assertEquals("Algo inesperado aconteceu", response.body!!.message)
    }

    @Test
    fun `handleGenericException should use default message when null`() {
        val ex = mockk<Exception>()
        every { ex.message } returns null

        val response = handler.handleGenericException(ex, request)

        assertEquals("Um erro inesperado ocorreu", response.body!!.message)
    }

    @Test
    fun `all handlers should include timestamp in response`() {
        val ex = InvalidCredentialsException("test")
        val response = handler.handleInvalidCredentials(ex, request)
        assertNotNull(response.body!!.timestamp)
        assertTrue(response.body!!.timestamp.isNotBlank())
    }

    @Test
    fun `all handlers should include path from request`() {
        every { request.requestURI } returns "/auth/validate"
        val ex = InvalidTokenException("test")
        val response = handler.handleInvalidToken(ex, request)
        assertEquals("/auth/validate", response.body!!.path)
    }
}
