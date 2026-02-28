package br.com.fiapx.auth.infrastructure.client

import br.com.fiapx.auth.domain.exception.UserNotFoundException
import br.com.fiapx.auth.domain.exception.UserServiceException
import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.service.UserService
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
    
    override fun findByEmail(email: String): User {
        return try {
            webClient.get()
                .uri("/users/by-email/{email}", email)
                .retrieve()
                .onStatus(
                    { it == HttpStatus.NOT_FOUND },
                    { Mono.error(UserNotFoundException("Usuário com email [$email] não encontrado")) }
                )
                .bodyToMono(UserResponse::class.java)
                .timeout(Duration.ofSeconds(5))
                .map { it.toDomain() }
                .block() ?: throw UserServiceException("Falhou em recuperar o usuário")
        } catch (e: WebClientResponseException.NotFound) {
            throw UserNotFoundException("Usuário com email [$email] não encontrado")
        } catch (e: UserNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw UserServiceException("Erro ao comunicar com o Serviço do Usuário: ${e.message}")
        }
    }
    
    override fun findById(id: UUID): User {
        return try {
            webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(
                    { it == HttpStatus.NOT_FOUND },
                    { Mono.error(UserNotFoundException("Usuário com ID [$id] não encontrado")) }
                )
                .bodyToMono(UserResponse::class.java)
                .timeout(Duration.ofSeconds(5))
                .map { it.toDomain() }
                .block() ?: throw UserServiceException("Falhou em recuperar o usuário")
        } catch (e: WebClientResponseException.NotFound) {
            throw UserNotFoundException("Usuário com ID [$id] não encontrado")
        } catch (e: UserNotFoundException) {
            throw e
        } catch (e: Exception) {
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
