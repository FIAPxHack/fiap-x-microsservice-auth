package br.com.fiapx.auth.infrastructure.client

import br.com.fiapx.auth.domain.exception.UserNotFoundException
import br.com.fiapx.auth.domain.exception.UserServiceException
import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.UUID

@Component
class UserServiceClient(
    private val webClient: WebClient
) : UserService {
    
    private val logger = LoggerFactory.getLogger(UserServiceClient::class.java)

    override fun findByEmail(email: String): User {
        logger.debug("[USER_SERVICE_CLIENT] iniciando busca de usuário por e-mail email={}", email)
        return try {
            val user = webClient.get()
                .uri("/api/users/by-email/{email}", email)
                .retrieve()
                .onStatus(
                    { it == HttpStatus.NOT_FOUND },
                    {
                        logger.debug("[USER_SERVICE_CLIENT] usuário não encontrado no serviço externo por e-mail email={}", email)
                        Mono.error(UserNotFoundException("Usuário com email [$email] não encontrado"))
                    }
                )
                .bodyToMono(UserResponse::class.java)
                .timeout(Duration.ofSeconds(5))
                .map { it.toDomain() }
                .block() ?: throw UserServiceException("Falhou em recuperar o usuário")

            logger.debug("[USER_SERVICE_CLIENT] usuário recuperado com sucesso por e-mail id={} email={}", user.id, user.email)
            user
        } catch (e: WebClientResponseException.NotFound) {
            logger.debug("[USER_SERVICE_CLIENT] resposta 404 ao buscar usuário por e-mail email={}", email, e)
            throw UserNotFoundException("Usuário com email [$email] não encontrado")
        } catch (e: UserNotFoundException) {
            logger.debug("[USER_SERVICE_CLIENT] usuário não encontrado por e-mail email={}", email, e)
            throw e
        } catch (e: Exception) {
            logger.debug("[USER_SERVICE_CLIENT] erro ao comunicar com o serviço de usuário na busca por e-mail email={}", email, e)
            throw UserServiceException("Erro ao comunicar com o Serviço do Usuário: ${e.message}")
        }
    }
    
    override fun findById(id: UUID): User {
        logger.debug("[USER_SERVICE_CLIENT]   iniciando busca de usuário por ID id={}", id)
        return try {
            val user = webClient.get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .onStatus(
                    { it == HttpStatus.NOT_FOUND },
                    {
                        logger.debug("[USER_SERVICE_CLIENT] usuário não encontrado no serviço externo por ID id={}", id)
                        Mono.error(UserNotFoundException("Usuário com ID [$id] não encontrado"))
                    }
                )
                .bodyToMono(UserResponse::class.java)
                .timeout(Duration.ofSeconds(5))
                .map { it.toDomain() }
                .block() ?: throw UserServiceException("Falhou em recuperar o usuário")

            logger.debug("[USER_SERVICE_CLIENT] usuário recuperado com sucesso por ID id={} email={}", user.id, user.email)
            user
        } catch (e: WebClientResponseException.NotFound) {
            logger.debug("[USER_SERVICE_CLIENT] resposta 404 ao buscar usuário por ID id={}", id, e)
            throw UserNotFoundException("Usuário com ID [$id] não encontrado")
        } catch (e: UserNotFoundException) {
            logger.debug("[USER_SERVICE_CLIENT] usuário não encontrado por ID id={}", id, e)
            throw e
        } catch (e: Exception) {
            logger.debug("[USER_SERVICE_CLIENT] erro ao comunicar com o serviço de usuário na busca por ID id={}", id, e)
            throw UserServiceException("Erro ao comunicar com o Serviço do Usuário: ${e.message}")
        }
    }

    private data class UserResponse(
        val id: UUID,
        val email: String,
        val passwordHash: String,
        val role: String
    ) {
        fun toDomain(): User = User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            role = role
        )
    }
}
